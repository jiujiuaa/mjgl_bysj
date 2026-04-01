package com.zjb.mjgl.service.impl;

import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.common.enums.MoldScrapStatusEnum;
import com.zjb.mjgl.common.enums.MoldStatusEnum;
import com.zjb.mjgl.common.enums.RoleEnum;
import com.zjb.mjgl.mapper.MoldQrcodesMapper;
import com.zjb.mjgl.mapper.MoldScrapApplicationsMapper;
import com.zjb.mjgl.mapper.MoldsMapper;
import com.zjb.mjgl.mapper.UseRecordMapper;
import com.zjb.mjgl.pojo.dto.MoldScrapApplicationDTO;
import com.zjb.mjgl.pojo.entity.MoldQrcodes;
import com.zjb.mjgl.pojo.entity.MoldScrapApplications;
import com.zjb.mjgl.pojo.entity.Molds;
import com.zjb.mjgl.pojo.entity.User;
import com.zjb.mjgl.pojo.vo.MoldScrapApplicationDetailVO;
import com.zjb.mjgl.pojo.vo.MoldScrapApplicationVO;
import com.zjb.mjgl.pojo.vo.MoldScrapTimelineItemVO;
import com.zjb.mjgl.service.MoldScrapApplicationsService;
import com.zjb.mjgl.utils.IdUtil;
import com.zjb.mjgl.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MoldScrapApplicationsServiceImpl implements MoldScrapApplicationsService {

    private final MoldScrapApplicationsMapper moldScrapApplicationsMapper;
    private final MoldsMapper moldsMapper;
    private final UseRecordMapper useRecordMapper;
    private final MoldQrcodesMapper moldQrcodesMapper;

    @Override
    public Result<String> create(MoldScrapApplicationDTO dto) {
        User currentUser = UserUtils.getCurrentUserDetails();
        if (currentUser == null) {
            return Result.fail("未登录用户无法创建报废申请");
        }
        RoleEnum role = currentUser.getRole();
        boolean canCreate = role == RoleEnum.ADMIN || role == RoleEnum.INSPECTOR || role == RoleEnum.PRODUCTION;
        if (!canCreate) {
            return Result.fail("当前用户无报废申请创建权限");
        }

        if (dto == null || dto.getMoldId() == null || dto.getMoldId().trim().isEmpty()) {
            return Result.fail("模具ID不能为空");
        }
        if (dto.getReason() == null || dto.getReason().trim().isEmpty()) {
            return Result.fail("申请原因不能为空");
        }

        String moldId = dto.getMoldId().trim();
        Integer currentStatus = moldsMapper.getStatus(moldId);
        if (MoldStatusEnum.TO_BE_SCRAPPED.getCode().equals(currentStatus)) {
            return Result.fail("模具已处于“待报废”状态");
        }

        MoldScrapApplications application = new MoldScrapApplications();
        application.setId(IdUtil.fastUUID());
        application.setMoldId(moldId);
        application.setApplicantId(currentUser.getId());
        application.setApplicantName(
                Optional.ofNullable(currentUser.getRealName())
                        .filter(s -> !s.trim().isEmpty())
                        .orElse(currentUser.getUsername())
        );
        application.setReason(dto.getReason().trim());
        application.setStatus(MoldScrapStatusEnum.PENDING_APPROVAL.getCode());

        LocalDateTime now = LocalDateTime.now();
        application.setCreatedAt(now);
        application.setUpdatedAt(now);

        int rows = moldScrapApplicationsMapper.insert(application);
        if (rows <= 0) {
            return Result.fail("创建报废申请失败");
        }

        // 关键规则：创建后立刻置为“待报废”，从而禁止继续新增使用/保养
        moldsMapper.updateStatus(moldId, MoldStatusEnum.TO_BE_SCRAPPED.getCode());

        return Result.success(application.getId());
    }

    @Override
    public Result<?> approve(String id, Integer approvalStatus, String comment) {
        if (id == null || id.trim().isEmpty()) {
            return Result.fail("报废申请ID不能为空");
        }
        if (approvalStatus == null) {
            return Result.fail("approvalStatus不能为空");
        }
        if (!Objects.equals(approvalStatus, MoldScrapStatusEnum.APPROVED.getCode())
                && !Objects.equals(approvalStatus, MoldScrapStatusEnum.REJECTED.getCode())) {
            return Result.fail("approvalStatus不合法");
        }

        User currentUser = UserUtils.getCurrentUserDetails();
        if (currentUser == null) {
            return Result.fail("未登录用户无法审批");
        }
        if (currentUser.getRole() != RoleEnum.ADMIN) {
            return Result.fail("仅管理员可审批报废申请");
        }

        MoldScrapApplications existing = Optional.ofNullable(moldScrapApplicationsMapper.selectById(id.trim()))
                .orElse(null);
        if (existing == null) {
            return Result.fail("报废申请不存在");
        }
        if (!Objects.equals(existing.getStatus(), MoldScrapStatusEnum.PENDING_APPROVAL.getCode())) {
            return Result.fail("当前申请已进入非待审批状态，无法重复审批");
        }

        String trimmedComment = Optional.ofNullable(comment)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .orElse(null);

        LocalDateTime now = LocalDateTime.now();
        int rows = moldScrapApplicationsMapper.updateApproval(
                id.trim(),
                approvalStatus,
                trimmedComment,
                currentUser.getId(),
                Optional.ofNullable(currentUser.getRealName())
                        .filter(s -> !s.trim().isEmpty())
                        .orElse(currentUser.getUsername()),
                now
        );
        if (rows <= 0) {
            return Result.fail("审批更新失败");
        }

        // 如果拒绝：恢复为 usage 推导出的状态（不考虑维修中的冲突，避免引入复杂查询）
        if (Objects.equals(approvalStatus, MoldScrapStatusEnum.REJECTED.getCode())) {
            int restored = computeMoldStatusFromUsage(existing.getMoldId());
            moldsMapper.updateStatus(existing.getMoldId(), restored);
        }

        return Result.success();
    }

    @Override
    public Result<?> execute(String id, String handlerComment) {
        if (id == null || id.trim().isEmpty()) {
            return Result.fail("报废申请ID不能为空");
        }
        User currentUser = UserUtils.getCurrentUserDetails();
        if (currentUser == null) {
            return Result.fail("未登录用户无法执行报废");
        }
        RoleEnum role = currentUser.getRole();
        boolean canExecute = role == RoleEnum.ADMIN || role == RoleEnum.INSPECTOR;
        if (!canExecute) {
            return Result.fail("当前用户无报废执行权限");
        }

        MoldScrapApplications existing = Optional.ofNullable(moldScrapApplicationsMapper.selectById(id.trim()))
                .orElse(null);
        if (existing == null) {
            return Result.fail("报废申请不存在");
        }
        if (!Objects.equals(existing.getStatus(), MoldScrapStatusEnum.APPROVED.getCode())) {
            return Result.fail("仅已批准状态允许执行");
        }

        String trimmedComment = Optional.ofNullable(handlerComment)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .orElse(null);

        LocalDateTime now = LocalDateTime.now();
        int rows = moldScrapApplicationsMapper.updateExecute(
                id.trim(),
                MoldScrapStatusEnum.EXECUTED.getCode(),
                currentUser.getId(),
                Optional.ofNullable(currentUser.getRealName())
                        .filter(s -> !s.trim().isEmpty())
                        .orElse(currentUser.getUsername()),
                trimmedComment,
                now
        );
        if (rows <= 0) {
            return Result.fail("报废执行更新失败");
        }

        // 执行报废：停用该模具的二维码，避免继续通过移动端产生操作
        List<MoldQrcodes> codes = Optional.ofNullable(moldQrcodesMapper.selectByMoldId(existing.getMoldId()))
                .orElseGet(Collections::emptyList);
        codes.stream()
                .filter(Objects::nonNull)
                .filter(c -> c.getId() != null && !c.getId().trim().isEmpty())
                .forEach(c -> {
                    try {
                        moldQrcodesMapper.updateIsActiveById(c.getId(), 0);
                    } catch (Exception e) {
                        log.warn("停用二维码失败, qrcodeId={}, err={}", c.getId(), e.getMessage());
                    }
                });

        // 保持 molds.current_status = 待报废(5)
        moldsMapper.updateStatus(existing.getMoldId(), MoldStatusEnum.TO_BE_SCRAPPED.getCode());
        return Result.success();
    }

    @Override
    public Result<MoldScrapApplicationDetailVO> getDetail(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Result.fail("报废申请ID不能为空");
        }
        MoldScrapApplications existing = moldScrapApplicationsMapper.selectById(id.trim());
        if (existing == null) {
            return Result.fail("报废申请不存在");
        }

        Molds mold = moldsMapper.selectById(existing.getMoldId());
        MoldScrapApplicationVO vo = new MoldScrapApplicationVO();
        vo.setId(existing.getId());
        vo.setMoldId(existing.getMoldId());
        vo.setMoldCode(mold != null ? mold.getMoldCode() : null);
        vo.setMoldName(mold != null ? mold.getName() : null);
        vo.setReason(existing.getReason());
        vo.setStatus(existing.getStatus());
        MoldScrapStatusEnum statusEnum = MoldScrapStatusEnum.fromCode(existing.getStatus());
        vo.setStatusDesc(statusEnum != null ? statusEnum.getDescription() : null);
        vo.setApplicantName(existing.getApplicantName());
        vo.setApproverName(existing.getApproverName());
        vo.setHandlerName(existing.getHandlerName());
        vo.setApprovalComment(existing.getApprovalComment());
        vo.setHandlerComment(existing.getHandlerComment());
        vo.setCreatedAt(existing.getCreatedAt());
        vo.setApprovedAt(existing.getApprovedAt());
        vo.setHandledAt(existing.getHandledAt());

        List<MoldScrapTimelineItemVO> timeline = buildTimeline(existing);

        MoldScrapApplicationDetailVO detailVO = new MoldScrapApplicationDetailVO();
        detailVO.setApplication(vo);
        detailVO.setTimeline(timeline);
        return Result.success(detailVO);
    }

    @Override
    public Result<List<MoldScrapApplicationVO>> listByCondition(String moldId, Integer status) {
        List<MoldScrapApplicationVO> list = Optional.ofNullable(
                        moldScrapApplicationsMapper.listByCondition(
                                moldId == null || moldId.trim().isEmpty() ? null : moldId.trim(),
                                status))
                .orElseGet(Collections::emptyList);

        list.forEach(vo -> {
            MoldScrapStatusEnum e = MoldScrapStatusEnum.fromCode(vo.getStatus());
            vo.setStatusDesc(e != null ? e.getDescription() : null);
        });

        // 默认按创建时间倒序（不依赖 mapper 的排序实现）
        list.sort(Comparator.comparing(MoldScrapApplicationVO::getCreatedAt,
                Comparator.nullsLast(Comparator.naturalOrder())).reversed());

        return Result.success(list);
    }

    private List<MoldScrapTimelineItemVO> buildTimeline(MoldScrapApplications existing) {
        if (existing == null) return Collections.emptyList();

        MoldScrapTimelineItemVO submitted = new MoldScrapTimelineItemVO();
        submitted.setEventType("SUBMITTED");
        submitted.setEventTypeDesc("申请提交");
        submitted.setOperatorName(existing.getApplicantName());
        submitted.setEventTime(existing.getCreatedAt());
        submitted.setComment(existing.getReason());

        MoldScrapTimelineItemVO approved = null;
        if (Objects.equals(existing.getStatus(), MoldScrapStatusEnum.APPROVED.getCode())) {
            approved = new MoldScrapTimelineItemVO();
            approved.setEventType("APPROVED");
            approved.setEventTypeDesc("审批通过");
            approved.setOperatorName(existing.getApproverName());
            approved.setEventTime(existing.getApprovedAt());
            approved.setComment(existing.getApprovalComment());
        }

        MoldScrapTimelineItemVO rejected = null;
        if (Objects.equals(existing.getStatus(), MoldScrapStatusEnum.REJECTED.getCode())) {
            rejected = new MoldScrapTimelineItemVO();
            rejected.setEventType("REJECTED");
            rejected.setEventTypeDesc("审批拒绝");
            rejected.setOperatorName(existing.getApproverName());
            rejected.setEventTime(existing.getApprovedAt());
            rejected.setComment(existing.getApprovalComment());
        }

        MoldScrapTimelineItemVO executed = null;
        if (Objects.equals(existing.getStatus(), MoldScrapStatusEnum.EXECUTED.getCode())) {
            executed = new MoldScrapTimelineItemVO();
            executed.setEventType("EXECUTED");
            executed.setEventTypeDesc("报废执行完成");
            executed.setOperatorName(existing.getHandlerName());
            executed.setEventTime(existing.getHandledAt());
            executed.setComment(existing.getHandlerComment());
        }

        List<MoldScrapTimelineItemVO> timeline = new ArrayList<>();
        if (submitted != null) timeline.add(submitted);
        if (approved != null) timeline.add(approved);
        if (rejected != null) timeline.add(rejected);
        if (executed != null) timeline.add(executed);

        timeline.sort(Comparator.comparing(MoldScrapTimelineItemVO::getEventTime,
                Comparator.nullsLast(Comparator.naturalOrder())));

        return timeline;
    }

    private int computeMoldStatusFromUsage(String moldId) {
        if (moldId == null) return MoldStatusEnum.IN_STOCK.getCode();
        String trimmed = moldId.trim();
        if (trimmed.isEmpty()) return MoldStatusEnum.IN_STOCK.getCode();

        int lentOutCount = useRecordMapper.countInUseByMoldIdAndUsageType(trimmed, 2);
        if (lentOutCount > 0) {
            return MoldStatusEnum.LENT_OUT.getCode();
        }

        int inUseCount = useRecordMapper.countInUseByMoldIdAndUsageType(trimmed, 1)
                + useRecordMapper.countInUseByMoldIdAndUsageType(trimmed, 3);
        return inUseCount > 0 ? MoldStatusEnum.IN_USE.getCode() : MoldStatusEnum.IN_STOCK.getCode();
    }
}

