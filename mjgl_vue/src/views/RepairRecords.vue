<template>
  <div class="repair-records-container">
    <!-- 全局左侧边栏 -->
    <AppSidebar />

    <!-- 右侧主区域 -->
    <div class="layout-main" v-back-to-top>
      <header class="top-header">
        <div class="top-title">模具维修记录</div>
        <div class="top-subtitle">记录模具从报修到验收的全流程</div>
      </header>

      <main class="main-content">
        <div class="content-wrapper">
          <section>
            <div class="card">
              <div class="card-header">
                <h3 class="card-title">维修记录列表</h3>
                <button
                  class="primary-btn"
                  type="button"
                  @click="handleShowCreateDialog"
                >
                  新建维修
                </button>
              </div>
              <div class="card-body">
                <div v-if="successMessage" class="success-message">
                  <span class="message-icon">✓</span>
                  {{ successMessage }}
                </div>
                <div v-if="errorMessage" class="error-message">
                  <span class="message-icon">⚠</span>
                  {{ errorMessage }}
                </div>

                <!-- 查询条件：与后端 RepairQueryParam 对应 -->
                <div class="query-form">
                  <div class="query-row">
                    <div class="query-item">
                      <label>选择模具</label>
                      <select v-model="query.moldId" class="form-input query-input">
                        <option value="">全部</option>
                        <option v-for="m in moldOptions" :key="m.id" :value="m.id">
                          {{ (m.moldCode || '') + (m.name ? ` - ${m.name}` : '') || m.id }}
                        </option>
                      </select>
                    </div>
                    <div class="query-item">
                      <label>模具名称/编号</label>
                      <input
                        v-model="query.keyword"
                        type="text"
                        class="form-input query-input"
                        placeholder="支持名称或编号模糊匹配"
                      />
                    </div>
                    <div class="query-item">
                      <label>维修状态</label>
                      <select v-model="query.status" class="form-input query-input">
                        <option :value="null">全部</option>
                        <option :value="1">待处理</option>
                        <option :value="2">维修中</option>
                        <option :value="3">已修复</option>
                        <option :value="4">已验收</option>
                      </select>
                    </div>
                    <div class="query-item">
                      <label>送修人</label>
                      <select v-model="query.reporterId" class="form-input query-input">
                        <option value="">全部</option>
                        <option v-for="u in userOptions" :key="u.id" :value="u.id">{{ u.realName || u.username }}</option>
                      </select>
                    </div>
                    <div class="query-item">
                      <label>维修人</label>
                      <select v-model="query.maintainerId" class="form-input query-input">
                        <option value="">全部</option>
                        <option v-for="u in userOptions" :key="'m-' + u.id" :value="u.id">{{ u.realName || u.username }}</option>
                      </select>
                    </div>
                    <div class="query-item">
                      <label>验证人</label>
                      <select v-model="query.verifierId" class="form-input query-input">
                        <option value="">全部</option>
                        <option v-for="u in userOptions" :key="'v-' + u.id" :value="u.id">{{ u.realName || u.username }}</option>
                      </select>
                    </div>
                  </div>
                  <div class="query-row">
                    <div class="query-item">
                      <label>开始时间</label>
                      <input
                        v-model="query.startTime"
                        type="datetime-local"
                        class="form-input query-input"
                      />
                    </div>
                    <div class="query-item">
                      <label>结束时间</label>
                      <input
                        v-model="query.endTime"
                        type="datetime-local"
                        class="form-input query-input"
                      />
                    </div>
                  </div>
                  <div class="query-actions">
                    <button type="button" class="primary-btn" @click="handleQuery">
                      查询
                    </button>
                    <button type="button" class="secondary-btn" @click="handleReset">
                      重置
                    </button>
                  </div>
                </div>

                <!-- 从模具页跳转过来时仅显示该模具的维修记录，无记录则列表为空 -->
                <div v-if="query.moldId" class="mold-filter-hint">
                  <span>当前仅显示该模具的维修记录（共 {{ repairPage.total }} 条）</span>
                  <button type="button" class="link-btn" @click="clearMoldFilter">清除模具筛选</button>
                </div>

                <div v-if="listLoading" class="table-loading">维修记录加载中...</div>
                <div v-else>
                  <div class="table-wrapper">
                    <table class="mold-table">
                      <thead>
                        <tr>
                          <th>模具编号</th>
                          <th>模具名称</th>
                          <th>类别</th>
                          <th>故障原因</th>
                          <th>送修人</th>
                          <th>维修人</th>
                          <th>验证人</th>
                          <th>开始时间</th>
                          <th>结束时间</th>
                          <th>状态</th>
                          <th>维修费用(元)</th>
                          <th>合理性审批</th>
                          <th>创建时间</th>
                          <th>操作</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-if="!repairPage.list || repairPage.list.length === 0">
                          <td colspan="15" class="empty-cell">暂无维修记录</td>
                        </tr>
                        <tr
                          v-for="record in repairPage.list"
                          :key="record.id"
                        >
                          <td>{{ record.moldCode }}</td>
                          <td>{{ record.moldName }}</td>
                          <td>{{ record.moldCategory || '-' }}</td>
                          <td>{{ record.repairReason || '-' }}</td>
                          <td>{{ record.reporterName || '-' }}</td>
                          <td>{{ record.maintainerName || '-' }}</td>
                          <td>{{ record.verifierName || '-' }}</td>
                          <td>{{ formatDate(record.startTime) }}</td>
                          <td>{{ formatDate(record.endTime) }}</td>
                          <td>
                            <span :class="getStatusClass(record.status)">
                              {{ record.statusDesc || formatStatus(record.status) }}
                            </span>
                          </td>
                          <td>{{ record.cost ?? '-' }}</td>
                          <td>
                            <span
                              v-if="record.repairApprovalStatus === 1"
                              class="status-normal"
                            >
                              合理
                            </span>
                            <span
                              v-else-if="record.repairApprovalStatus === 2"
                              class="status-danger"
                            >
                              存在问题
                            </span>
                            <span v-else>-</span>
                          </td>
                          <td>{{ formatDate(record.createdAt) }}</td>
                          <td>
                            <div class="action-buttons">
                              <button
                                class="action-btn"
                                @click="openDetailDialog(record)"
                              >
                                详情
                              </button>
                              <button
                                v-if="isAdmin && record.status === 4"
                                class="action-btn status-btn"
                                :disabled="rowLoadingId === record.id"
                                @click="openApprovalDialog(record)"
                              >
                                审批
                              </button>
                              <button
                                v-if="record.status === 1"
                                class="action-btn status-btn"
                                :disabled="rowLoadingId === record.id"
                                @click="handleChangeStatus(record, 2)"
                              >
                                开始维修
                              </button>
                              <button
                                v-else-if="record.status === 2"
                                class="action-btn status-btn"
                                :disabled="rowLoadingId === record.id"
                                @click="handleFinish(record)"
                              >
                                结束维修
                              </button>
                              <button
                                v-else-if="record.status === 3"
                                class="action-btn status-btn"
                                :disabled="rowLoadingId === record.id"
                                @click="openAcceptDialog(record)"
                              >
                                验收
                              </button>
                              <button
                                class="action-btn delete-btn"
                                :disabled="rowLoadingId === record.id"
                                @click="handleDeleteRecord(record)"
                              >
                                删除
                              </button>
                            </div>
                          </td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                  <!-- 分页 -->
                  <div v-if="repairPage.pages && repairPage.pages > 1" class="pagination">
                    <button
                      class="page-btn"
                      :disabled="pageNum === 1"
                      @click="changePage(pageNum - 1)"
                    >
                      上一页
                    </button>
                    <span class="page-info">
                      第 {{ pageNum }} / {{ repairPage.pages }} 页，
                      共 {{ repairPage.total || 0 }} 条
                    </span>
                    <button
                      class="page-btn"
                      :disabled="pageNum === repairPage.pages"
                      @click="changePage(pageNum + 1)"
                    >
                      下一页
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </section>
        </div>
      </main>
    </div>

    <!-- 新建维修记录对话框：选择模具 + 故障原因 + 现场图片 -->
    <div
      v-if="showCreateDialog"
      class="dialog-overlay"
      @click="closeCreateDialog"
    >
      <div class="dialog-content" @click.stop>
        <div class="dialog-header">
          <h3>新建维修记录</h3>
          <button class="dialog-close" @click="closeCreateDialog">×</button>
        </div>
        <div class="dialog-body">
          <form class="mold-form" @submit.prevent="handleCreate">
            <div class="form-section-title">基础信息</div>
            <p class="mold-info-text">
              模具：{{ createForm.moldName || '-' }} / {{ createForm.moldCode || '-' }} /
              {{ createForm.moldCategory || '-' }}
            </p>
            <div class="form-row">
              <div class="form-group">
                <label for="createMoldSelect">选择模具 *</label>
                <select
                  id="createMoldSelect"
                  v-model="createForm.moldId"
                  class="form-input"
                  @change="handleCreateMoldChange"
                >
                  <option value="">请选择模具</option>
                  <option
                    v-for="m in moldOptions"
                    :key="m.id"
                    :value="m.id"
                  >
                    {{ m.moldCode }} - {{ m.name }}
                  </option>
                </select>
              </div>
            </div>

            <div class="form-section-title">备注</div>
            <div class="form-group">
              <label for="createNotes">备注</label>
              <input
                id="createNotes"
                v-model="createForm.notes"
                type="text"
                class="form-input"
                placeholder="可填写初步判断、处理建议等"
              />
            </div>

            <div class="form-section-title">现场图片（可选）</div>
            <div class="form-row">
              <div class="form-group">
                <label for="createFileDescription">文件说明</label>
                <input
                  id="createFileDescription"
                  v-model="createUploadDescription"
                  type="text"
                  class="form-input"
                  placeholder="例如：报修现场照片"
                />
              </div>
            </div>
            <div class="form-group">
              <label for="createFiles">选择图片（可多选）</label>
              <input
                id="createFiles"
                type="file"
                multiple
                accept="image/*"
                class="form-input"
                @change="handleCreateFilesChange"
              />
              <div
                v-if="createSelectedFiles.length"
                class="file-list selected-file-list"
              >
                <div
                  v-for="file in createSelectedFiles"
                  :key="file.name + file.size + file.lastModified"
                  class="file-item"
                >
                  <div class="file-main">
                    <span class="file-name">{{ file.name }}</span>
                    <span class="file-meta">
                      {{ (file.size / 1024).toFixed(1) }} KB
                    </span>
                  </div>
                  <button
                    type="button"
                    class="file-preview-btn"
                    @click="previewSelectedFile(file)"
                  >
                    预览
                  </button>
                  <button
                    type="button"
                    class="file-delete-btn"
                    @click="removeCreateSelectedFile(file)"
                  >
                    删除
                  </button>
                </div>
              </div>
            </div>

            <div v-if="createDialogErrorMessage" class="error-message">
              <span class="message-icon">⚠</span>
              {{ createDialogErrorMessage }}
            </div>

            <div class="dialog-actions">
              <button
                type="submit"
                class="submit-button"
                :disabled="createLoading"
              >
                {{ createLoading ? '提交中...' : '提交' }}
              </button>
              <button
                type="button"
                class="cancel-button"
                @click="closeCreateDialog"
              >
                取消
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- 编辑 / 结束维修 对话框（维护原因/费用/备注，以及结束维修时的图片） -->
    <div v-if="showEditDialog" class="dialog-overlay" @click="closeEditDialog">
      <div class="dialog-content" @click.stop>
        <div class="dialog-header">
          <h3>编辑维修记录</h3>
          <button class="dialog-close" @click="closeEditDialog">×</button>
        </div>
        <div class="dialog-body">
          <form class="mold-form" @submit.prevent="handleSave">
            <div class="form-section-title">基本信息</div>
            <p class="mold-info-text">
              模具：{{ editForm.moldName || '-' }} / {{ editForm.moldCode || '-' }} /
              {{ editForm.moldCategory || '-' }}
            </p>

            <div class="form-group">
              <label for="editRepairReason">故障原因</label>
              <textarea
                id="editRepairReason"
                v-model="editForm.repairReason"
                class="form-input"
                rows="3"
              ></textarea>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label for="editCost">维修费用(元)</label>
                <input
                  id="editCost"
                  v-model.number="editForm.cost"
                  type="number"
                  min="0"
                  step="0.01"
                  class="form-input"
                />
              </div>
              <div class="form-group">
                <label for="editNotes">备注</label>
                <input
                  id="editNotes"
                  v-model="editForm.notes"
                  type="text"
                  class="form-input"
                />
              </div>
            </div>

            <!-- 结束维修模式下的图片上传 -->
            <template v-if="isFinishMode">
              <div class="form-section-title">维修完成图片（必传）</div>
              <div class="form-row">
                <div class="form-group">
                  <label for="finishFileDescription">文件说明</label>
                  <input
                    id="finishFileDescription"
                    v-model="finishUploadDescription"
                    type="text"
                    class="form-input"
                    placeholder="例如：维修过程/结果照片"
                  />
                </div>
              </div>
              <div class="form-group">
                <label for="finishFiles">选择图片（可多选）</label>
                <input
                  id="finishFiles"
                  type="file"
                  multiple
                  accept="image/*"
                  class="form-input"
                  @change="handleFinishFilesChange"
                />
                <div
                  v-if="finishSelectedFiles.length"
                  class="file-list selected-file-list"
                >
                  <div
                    v-for="file in finishSelectedFiles"
                    :key="file.name + file.size + file.lastModified"
                    class="file-item"
                  >
                    <div class="file-main">
                      <span class="file-name">{{ file.name }}</span>
                      <span class="file-meta">
                        {{ (file.size / 1024).toFixed(1) }} KB
                      </span>
                    </div>
                    <button
                      type="button"
                      class="file-preview-btn"
                      @click="previewSelectedFile(file)"
                    >
                      预览
                    </button>
                    <button
                      type="button"
                      class="file-delete-btn"
                      @click="removeFinishSelectedFile(file)"
                    >
                      删除
                    </button>
                  </div>
                </div>
              </div>
            </template>

            <div v-if="dialogErrorMessage" class="error-message">
              <span class="message-icon">⚠</span>
              {{ dialogErrorMessage }}
            </div>

            <div class="dialog-actions">
              <template v-if="isFinishMode">
                <button
                  type="button"
                  class="submit-button"
                  :disabled="saveLoading"
                  @click="handleSave"
                >
                  {{ saveLoading ? '保存中...' : '仅保存' }}
                </button>
                <button
                  type="button"
                  class="submit-button"
                  :disabled="saveLoading"
                  @click="handleSaveAndFinish"
                >
                  {{ saveLoading ? '处理中...' : '保存并结束维修' }}
                </button>
              </template>
              <template v-else>
                <button
                  type="submit"
                  class="submit-button"
                  :disabled="saveLoading"
                >
                  {{ saveLoading ? '保存中...' : '保存' }}
                </button>
              </template>
              <button
                type="button"
                class="cancel-button"
                @click="closeEditDialog"
              >
                取消
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- 验收对话框 -->
    <div
      v-if="showAcceptDialog"
      class="dialog-overlay"
      @click="closeAcceptDialog"
    >
      <div class="dialog-content" @click.stop>
        <div class="dialog-header">
          <h3>维修验收</h3>
          <button class="dialog-close" @click="closeAcceptDialog">×</button>
        </div>
        <div class="dialog-body">
          <form class="mold-form" @submit.prevent="handleAccept">
            <div class="form-section-title">验收结果</div>
            <div class="form-group">
              <label>是否通过</label>
              <select v-model="acceptForm.decision" class="form-input">
                <option value="pass">通过</option>
                <option value="fail">不通过</option>
              </select>
            </div>
            <div class="form-group">
              <label for="acceptComment">说明</label>
              <input
                id="acceptComment"
                v-model="acceptForm.comment"
                type="text"
                class="form-input"
                placeholder="可填写验收结论或不通过原因"
              />
            </div>
            <div v-if="acceptDialogErrorMessage" class="error-message">
              <span class="message-icon">⚠</span>
              {{ acceptDialogErrorMessage }}
            </div>
            <div class="dialog-actions">
              <button
                type="button"
                class="submit-button"
                :disabled="acceptLoading"
                @click="handleAccept"
              >
                {{ acceptLoading ? '保存中...' : '仅保存' }}
              </button>
              <button
                type="button"
                class="submit-button"
                :disabled="acceptLoading"
                @click="handleAcceptAndApply"
              >
                {{ acceptLoading ? '提交中...' : '通过并完成验收' }}
              </button>
              <button
                type="button"
                class="cancel-button"
                @click="closeAcceptDialog"
              >
                取消
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- 详情对话框（只读查看当前记录所有已填写信息） -->
    <div
      v-if="showDetailDialog && detailRecord"
      class="dialog-overlay"
      @click="closeDetailDialog"
    >
      <div class="dialog-content" @click.stop>
        <div class="dialog-header">
          <h3>维修详情</h3>
          <button class="dialog-close" @click="closeDetailDialog">×</button>
        </div>
        <div class="dialog-body">
          <div class="mold-form">
            <div class="form-section-title">模具信息</div>
            <p class="mold-info-text">
              模具：{{ detailRecord.moldName || '-' }} / {{ detailRecord.moldCode || '-' }} /
              {{ detailRecord.moldCategory || '-' }}
            </p>

            <div class="form-section-title">人员信息</div>
            <div class="form-row">
              <div class="form-group">
                <label>送修人</label>
                <div class="form-input readonly-text">
                  {{ detailRecord.reporterName || '-' }}
                </div>
              </div>
              <div class="form-group">
                <label>维修人</label>
                <div class="form-input readonly-text">
                  {{ detailRecord.maintainerName || '-' }}
                </div>
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>验证人</label>
                <div class="form-input readonly-text">
                  {{ detailRecord.verifierName || '-' }}
                </div>
              </div>
              <div class="form-group">
                <label>维修状态</label>
                <div class="form-input readonly-text">
                  {{ detailRecord.statusDesc || formatStatus(detailRecord.status) }}
                </div>
              </div>
            </div>

            <div class="form-section-title">时间与费用</div>
            <div class="form-row">
              <div class="form-group">
                <label>开始时间</label>
                <div class="form-input readonly-text">
                  {{ formatDate(detailRecord.startTime) }}
                </div>
              </div>
              <div class="form-group">
                <label>结束时间</label>
                <div class="form-input readonly-text">
                  {{ formatDate(detailRecord.endTime) }}
                </div>
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>维修费用(元)</label>
                <div class="form-input readonly-text">
                  {{ detailRecord.cost ?? '-' }}
                </div>
              </div>
              <div class="form-group">
                <label>创建时间</label>
                <div class="form-input readonly-text">
                  {{ formatDate(detailRecord.createdAt) }}
                </div>
              </div>
            </div>

            <div class="form-section-title">内容</div>
            <div class="form-group">
              <label>故障原因</label>
              <div class="form-input readonly-text multiline">
                {{ detailRecord.repairReason || '-' }}
              </div>
            </div>
            <div class="form-group">
              <label>备注</label>
              <div class="form-input readonly-text multiline">
                {{ detailRecord.notes || '-' }}
              </div>
            </div>

            <div class="form-section-title">报修现场图片（模具维度）</div>
            <div v-if="!detailReportPhotos.length" class="no-files">
              暂无报修现场图片
            </div>
            <ul
              v-else
              class="file-list"
            >
              <li
                v-for="file in detailReportPhotos"
                :key="file.id"
                class="file-item"
              >
                <div class="file-main">
                  <span class="file-name">{{ file.originalName || file.filePath }}</span>
                  <span class="file-meta">
                    {{ file.fileType }} · {{ formatDate(file.uploadTime) }}
                  </span>
                </div>
                <button
                  type="button"
                  class="file-preview-btn"
                  @click="handlePreviewFile(file)"
                >
                  预览
                </button>
                <button
                  type="button"
                  class="file-delete-btn"
                  @click="handleDeleteDetailFile(file, 'REPORT')"
                >
                  删除
                </button>
              </li>
            </ul>

            <div class="form-section-title">维修完成图片（本次维修）</div>
            <div v-if="!detailRepairPhotos.length" class="no-files">
              暂无维修完成图片
            </div>
            <ul
              v-else
              class="file-list"
            >
              <li
                v-for="file in detailRepairPhotos"
                :key="file.id"
                class="file-item"
              >
                <div class="file-main">
                  <span class="file-name">{{ file.originalName || file.filePath }}</span>
                  <span class="file-meta">
                    {{ file.fileType }} · {{ formatDate(file.uploadTime) }}
                  </span>
                </div>
                <button
                  type="button"
                  class="file-preview-btn"
                  @click="handlePreviewFile(file)"
                >
                  预览
                </button>
                <button
                  type="button"
                  class="file-delete-btn"
                  @click="handleDeleteDetailFile(file, 'FINISH')"
                >
                  删除
                </button>
              </li>
            </ul>

            <div class="dialog-actions">
              <button
                type="button"
                class="cancel-button"
                @click="closeDetailDialog"
              >
                关闭
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 合理性审批对话框 -->
    <div v-if="showApprovalDialog" class="dialog-overlay" @click="closeApprovalDialog">
      <div class="dialog-content dialog-content-sm" @click.stop>
        <div class="dialog-header">
          <h3>合理性审批</h3>
          <button class="dialog-close" @click="closeApprovalDialog">×</button>
        </div>
        <div class="dialog-body">
          <form @submit.prevent="handleApprovalSubmit">
            <div class="form-group">
              <label for="approvalStatus">审批结果</label>
              <select
                id="approvalStatus"
                v-model.number="approvalForm.repairApprovalStatus"
                class="form-input"
              >
                <option :value="0">未审核</option>
                <option :value="1">合理</option>
                <option :value="2">存在问题</option>
              </select>
            </div>
            <div class="form-group">
              <label for="approvalComment">审批意见</label>
              <input
                id="approvalComment"
                v-model="approvalForm.repairApprovalComment"
                type="text"
                class="form-input"
                placeholder="可填写原因分析或改进建议"
              />
            </div>
            <div v-if="approvalDialogError" class="error-message">
              <span class="message-icon">⚠</span>
              {{ approvalDialogError }}
            </div>
            <div class="dialog-actions">
              <button type="submit" class="submit-button" :disabled="approvalLoading">
                {{ approvalLoading ? '提交中...' : '提交' }}
              </button>
              <button type="button" class="cancel-button" @click="closeApprovalDialog">取消</button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- 图片删除确认对话框 -->
    <div
      v-if="showImageConfirmDialog"
      class="dialog-overlay"
      @click="showImageConfirmDialog = false"
    >
      <div class="confirm-dialog" @click.stop>
        <div class="confirm-icon danger">
          🗑️
        </div>
        <h3 class="confirm-title">删除图片</h3>
        <p class="confirm-message">
          确定要删除图片
          “{{ deletingImage?.originalName || deletingImage?.filePath || '' }}”
          吗？此操作不可恢复！
        </p>
        <div class="confirm-actions">
          <button
            class="confirm-cancel-btn"
            @click="showImageConfirmDialog = false"
          >
            取消
          </button>
          <button
            class="confirm-ok-btn danger"
            @click="confirmDeleteDetailFile"
          >
            删除
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import AppSidebar from '@/components/AppSidebar.vue'
import { fetchMolds } from '@/api/molds'
import { uploadBizFiles, getFilePreviewUrl, fetchBizFiles, deleteFiles } from '@/api/files'
import {
  queryRepairRecords,
  createRepairRecord,
  updateRepairRecord,
  deleteRepairRecord,
  approveRepairRecord,
} from '@/api/repairRecords'
import { fetchAllUsers } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const showOpsChildren = ref(true)
const showMonitoringChildren = ref(true)

const listLoading = ref(false)
const successMessage = ref('')
const errorMessage = ref('')

const pageNum = ref(1)
const pageSize = ref(10)
const repairPage = reactive({
  list: [],
  total: 0,
  pages: 0,
})
const rowLoadingId = ref('')

const moldOptions = ref([])
const moldOptionsLoaded = ref(false)
const moldOptionsLoading = ref(false)
const userOptions = ref([])

const query = reactive({
  moldId: '',
  keyword: '',
  status: null,
  reporterId: '',
  maintainerId: '',
  verifierId: '',
  startTime: '',
  endTime: '',
})

const showCreateDialog = ref(false)
const createLoading = ref(false)
const createDialogErrorMessage = ref('')
const createSelectedFiles = ref([])
const createUploadDescription = ref('')

const showEditDialog = ref(false)
const saveLoading = ref(false)
const dialogErrorMessage = ref('')
const isFinishMode = ref(false)
const finishSelectedFiles = ref([])
const finishUploadDescription = ref('')

const createForm = reactive({
  moldId: '',
  moldCode: '',
  moldName: '',
  moldCategory: '',
  notes: '',
})

const editForm = reactive({
  id: '',
  moldId: '',
  moldCode: '',
  moldName: '',
  moldCategory: '',
  repairReason: '',
  cost: null,
  notes: '',
})

const buildQueryParam = () => {
  const startTime = query.startTime?.trim()
  const endTime = query.endTime?.trim()
  return {
    moldId: query.moldId?.trim() || undefined,
    keyword: query.keyword?.trim() || undefined,
    status: query.status ?? undefined,
    reporterId: query.reporterId?.trim() || undefined,
    maintainerId: query.maintainerId?.trim() || undefined,
    verifierId: query.verifierId?.trim() || undefined,
    startTime: (() => {
      if (!startTime) return undefined
      const s = startTime.replace('T', ' ').slice(0, 19)
      return s.length <= 16 ? `${s}:00` : s
    })(),
    endTime: (() => {
      if (!endTime) return undefined
      const s = endTime.replace('T', ' ').slice(0, 19)
      return s.length <= 16 ? `${s}:00` : s
    })(),
  }
}

const loadRecords = async () => {
  listLoading.value = true
  errorMessage.value = ''
  try {
    const res = await queryRepairRecords(buildQueryParam(), pageNum.value, pageSize.value)
    const data = res.data || {}
    repairPage.list = data.list ?? []
    repairPage.total = data.total ?? 0
    repairPage.pages = data.pages ?? 0
  } catch (e) {
    errorMessage.value = e.message || '加载维修记录失败'
  } finally {
    listLoading.value = false
  }
}

const handleQuery = () => {
  pageNum.value = 1
  loadRecords()
}

const handleReset = () => {
  query.moldId = ''
  query.keyword = ''
  query.status = null
  query.reporterId = ''
  query.maintainerId = ''
  query.verifierId = ''
  query.startTime = ''
  query.endTime = ''
  pageNum.value = 1
  loadRecords()
}

/** 仅清除模具筛选，显示全部维修记录 */
const clearMoldFilter = () => {
  query.moldId = ''
  pageNum.value = 1
  loadRecords()
}

const changePage = (newPage) => {
  if (newPage < 1 || (repairPage.pages && newPage > repairPage.pages)) return
  pageNum.value = newPage
  loadRecords()
}

const loadUserOptions = async () => {
  try {
    const res = await fetchAllUsers()
    userOptions.value = res.data || []
  } catch (e) {
    console.error(e)
  }
}

const loadMoldOptions = async () => {
  if (moldOptionsLoaded.value || moldOptionsLoading.value) return
  moldOptionsLoading.value = true
  try {
    const res = await fetchMolds(1, 1000)
    moldOptions.value = res.data?.list || []
    moldOptionsLoaded.value = true
  } catch (e) {
    console.error(e)
  } finally {
    moldOptionsLoading.value = false
  }
}

const syncCreateFormFromSelectedMold = () => {
  const mold = moldOptions.value.find((m) => m.id === createForm.moldId)
  if (mold) {
    createForm.moldCode = mold.moldCode
    createForm.moldName = mold.name
    createForm.moldCategory = mold.category || ''
  }
}

const handleShowCreateDialog = async () => {
  await loadMoldOptions()
  if (route.query.moldId) {
    createForm.moldId = String(route.query.moldId)
  } else if (!createForm.moldId && moldOptions.value.length === 1) {
    createForm.moldId = moldOptions.value[0].id
  }
  syncCreateFormFromSelectedMold()
  createForm.notes = ''
  createSelectedFiles.value = []
  createUploadDescription.value = ''
  createDialogErrorMessage.value = ''
  showCreateDialog.value = true
}

const handleCreateMoldChange = () => {
  syncCreateFormFromSelectedMold()
}

const handleCreateFilesChange = (event) => {
  const files = Array.from(event.target.files || [])
  createSelectedFiles.value = files
}

const removeCreateSelectedFile = (file) => {
  if (!file) return
  createSelectedFiles.value = createSelectedFiles.value.filter(
    (f) =>
      !(
        f.name === file.name &&
        f.size === file.size &&
        f.lastModified === file.lastModified
      ),
  )
}

const closeCreateDialog = () => {
  showCreateDialog.value = false
}

const handleCreate = async () => {
  createDialogErrorMessage.value = ''

  if (!createForm.moldId || !String(createForm.moldId).trim()) {
    createDialogErrorMessage.value = '模具ID不能为空'
    return
  }

  const payload = {
    moldId: createForm.moldId.trim(),
    notes: createForm.notes || null,
  }

  createLoading.value = true
  try {
    const res = await createRepairRecord(payload)
    const recordId = res.data

    // 现场图片：作为该维修记录的“报修阶段”附件上传（bizType=repair, fileType=repair_photo）
    if (recordId && createSelectedFiles.value.length) {
      await uploadBizFiles(recordId, createSelectedFiles.value, {
        bizType: 'repair',
        fileType: 'repair_photo',
        imageStatus: 'REPORT',
        description: createUploadDescription.value || '维修现场照片',
      })
    }

    successMessage.value = '新建维修记录成功'
    showCreateDialog.value = false
    await loadRecords()
    setTimeout(() => {
      successMessage.value = ''
    }, 3000)
  } catch (e) {
    createDialogErrorMessage.value = e.message || '新建维修记录失败'
  } finally {
    createLoading.value = false
  }
}

const closeEditDialog = () => {
  showEditDialog.value = false
  isFinishMode.value = false
  finishSelectedFiles.value = []
  finishUploadDescription.value = ''
}

const handleSave = async () => {
  dialogErrorMessage.value = ''
  if (!editForm.id || !editForm.moldId) {
    dialogErrorMessage.value = '记录ID或模具ID缺失，无法保存'
    return
  }
  const payload = {
    id: editForm.id,
    moldId: editForm.moldId,
    repairReason: editForm.repairReason ? editForm.repairReason.trim() : null,
    cost: editForm.cost != null ? editForm.cost : null,
    notes: editForm.notes || null,
  }
  saveLoading.value = true
  try {
    await updateRepairRecord(payload)
    successMessage.value = '更新维修记录成功'
    showEditDialog.value = false
    await loadRecords()
    setTimeout(() => {
      successMessage.value = ''
    }, 3000)
  } catch (e) {
    dialogErrorMessage.value = e.message || '更新维修记录失败'
  } finally {
    saveLoading.value = false
  }
}

const handleSaveAndFinish = async () => {
  dialogErrorMessage.value = ''
  if (!editForm.id || !editForm.moldId) {
    dialogErrorMessage.value = '记录ID或模具ID缺失，无法保存'
    return
  }
  if (!editForm.repairReason || !editForm.repairReason.trim()) {
    dialogErrorMessage.value = '结束维修时必须填写故障原因'
    return
  }
  if (!finishSelectedFiles.value.length) {
    dialogErrorMessage.value = '结束维修时至少上传一张维修图片'
    return
  }

  const payload = {
    id: editForm.id,
    moldId: editForm.moldId,
    repairReason: editForm.repairReason ? editForm.repairReason.trim() : null,
    cost: editForm.cost != null ? editForm.cost : null,
    notes: editForm.notes || null,
    status: 3,
  }

  saveLoading.value = true
  try {
    await updateRepairRecord(payload)

    // 结束维修时，将图片按维修记录维度保存
    await uploadBizFiles(editForm.id, finishSelectedFiles.value, {
      bizType: 'repair',
      fileType: 'repair_photo',
      imageStatus: 'FINISH',
      description: finishUploadDescription.value || '维修完成图片',
    })
    successMessage.value = '结束维修并保存成功'
    showEditDialog.value = false
    await loadRecords()
    setTimeout(() => {
      successMessage.value = ''
    }, 3000)
  } catch (e) {
    dialogErrorMessage.value = e.message || '结束维修失败'
  } finally {
    saveLoading.value = false
    isFinishMode.value = false
  }
}

const handleFinish = (record) => {
  dialogErrorMessage.value = ''
  isFinishMode.value = true
  finishSelectedFiles.value = []
  finishUploadDescription.value = ''
  editForm.id = record.id
  editForm.moldId = record.moldId
  editForm.moldCode = record.moldCode
  editForm.moldName = record.moldName
  editForm.moldCategory = record.moldCategory || ''
  editForm.repairReason = record.repairReason || ''
  editForm.cost = record.cost ?? null
  editForm.notes = record.notes || ''
  showEditDialog.value = true
}

const handleFinishFilesChange = (event) => {
  const files = Array.from(event.target.files || [])
  finishSelectedFiles.value = files
}

const removeFinishSelectedFile = (file) => {
  if (!file) return
  finishSelectedFiles.value = finishSelectedFiles.value.filter(
    (f) =>
      !(
        f.name === file.name &&
        f.size === file.size &&
        f.lastModified === file.lastModified
      ),
  )
}

// 获取当前登录用户ID（通过用户列表匹配用户名）
const currentUserId = ref('')
const ensureUserListLoaded = async () => {
  if (!authStore.users || !authStore.users.length) {
    await authStore.loadUsers()
  }
  const me = (authStore.users || []).find(
    (u) => u.username === authStore.username
  )
  currentUserId.value = me?.id || ''
}

const handleChangeStatus = async (record, targetStatus) => {
  if (!record || !record.id) return
  rowLoadingId.value = record.id
  try {
    const payload = {
      id: record.id,
      moldId: record.moldId,
      status: targetStatus,
    }

    // 进入“维修中”或“已验收”时补充维修人/验证人
    if (targetStatus === 2 || targetStatus === 4) {
      await ensureUserListLoaded()
      if (!currentUserId.value) {
        throw new Error('无法获取当前用户ID，请联系管理员配置用户信息')
      }
      if (targetStatus === 2) {
        payload.maintainerId = currentUserId.value
      } else if (targetStatus === 4) {
        payload.verifierId = currentUserId.value
      }
    }

    await updateRepairRecord(payload)
    successMessage.value =
      targetStatus === 2
        ? '已开始维修'
        : targetStatus === 3
          ? '已结束维修'
          : '已验收通过'
    await loadRecords()
    setTimeout(() => {
      successMessage.value = ''
    }, 3000)
  } catch (e) {
    errorMessage.value = e.message || '更新维修状态失败'
  } finally {
    rowLoadingId.value = ''
  }
}

const handleDeleteRecord = async (record) => {
  if (!record || !record.id) return
  const ok = window.confirm(
    `确定要删除该维修记录吗？此操作不可恢复！\n模具：${record.moldCode || ''} ${record.moldName || ''}`
  )
  if (!ok) return
  rowLoadingId.value = record.id
  try {
    await deleteRepairRecord(record.id)
    successMessage.value = '删除维修记录成功'
    await loadRecords()
    setTimeout(() => {
      successMessage.value = ''
    }, 3000)
  } catch (e) {
    errorMessage.value = e.message || '删除维修记录失败'
  } finally {
    rowLoadingId.value = ''
  }
}

const formatStatus = (status) => {
  if (status === null || status === undefined) return '-'
  const map = {
    1: '待处理',
    2: '维修中',
    3: '已修复',
    4: '已验收',
  }
  return map[status] || status
}

const getStatusClass = (status) => {
  if (status === 2) return 'status-warning'
  if (status === 3) return 'status-normal'
  if (status === 4) return 'status-normal'
  if (status === 1) return 'status-danger'
  return ''
}

const formatDate = (val) => {
  if (!val) return '-'
  if (typeof val === 'string' && val.includes(' ')) {
    return val
  }
  const date = new Date(val)
  if (Number.isNaN(date.getTime())) return val
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}

const goUserManagement = () => {
  router.push('/user-management')
}

const goMoldManagement = () => {
  router.push('/mold-management')
}

const goUseRecords = () => {
  router.push('/mold-use-records')
}

const goMaintenancePlans = () => {
  router.push('/maintenance-plans')
}

const goMaintenanceLogs = () => {
  router.push('/maintenance-logs')
}

const goMaintenanceReminders = () => {
  router.push('/maintenance-reminders')
}

const goMonitoringManual = () => {
  router.push('/monitoring-manual')
}

// 验收相关
const showAcceptDialog = ref(false)
const acceptLoading = ref(false)
const acceptDialogErrorMessage = ref('')
const acceptForm = reactive({
  id: '',
  moldId: '',
  decision: 'pass',
  comment: '',
})

const openAcceptDialog = (record) => {
  acceptDialogErrorMessage.value = ''
  acceptForm.id = record.id
  acceptForm.moldId = record.moldId
  acceptForm.decision = 'pass'
  acceptForm.comment = ''
  showAcceptDialog.value = true
}

const closeAcceptDialog = () => {
  showAcceptDialog.value = false
}

const handleAccept = async () => {
  acceptDialogErrorMessage.value = ''
  if (!acceptForm.id || !acceptForm.moldId) {
    acceptDialogErrorMessage.value = '记录ID或模具ID缺失，无法验收'
    return
  }
  acceptLoading.value = true
  try {
    await ensureUserListLoaded()
    if (!currentUserId.value) {
      throw new Error('无法获取当前用户ID，请联系管理员配置用户信息')
    }
    const basePayload = {
      id: acceptForm.id,
      moldId: acceptForm.moldId,
      verifierId: currentUserId.value,
    }
    const isPass = acceptForm.decision === 'pass'
    // 通过：只保存验收意见；不通过：回退状态到“维修中”(2)
    const payload = {
      ...basePayload,
      notes: acceptForm.comment || null,
      ...(isPass ? {} : { status: 2 }),
    }
    await updateRepairRecord(payload)

    // 验收不通过时，删除该维修记录已上传的“维修完成”图片（imageStatus = FINISH）
    if (!isPass) {
      try {
        const res = await fetchBizFiles('repair', acceptForm.id, 'repair_photo')
        const files = res.data || []
        const finishIds = files
          .filter((f) => f.imageStatus === 'FINISH')
          .map((f) => f.id)
        if (finishIds.length) {
          await deleteFiles(finishIds)
        }
      } catch (err) {
        console.error(err)
      }
    }

    successMessage.value = isPass
      ? '验收意见已保存（通过）'
      : '验收不通过，已退回维修中并清理完成图片'
    showAcceptDialog.value = false
    await loadRecords()
    setTimeout(() => {
      successMessage.value = ''
    }, 3000)
  } catch (e) {
    acceptDialogErrorMessage.value = e.message || '提交验收结果失败'
  } finally {
    acceptLoading.value = false
  }
}

// 详情查看（只读）
const showDetailDialog = ref(false)
const detailRecord = ref(null)

const isAdmin = computed(() => authStore.role === 'ADMIN')

const showApprovalDialog = ref(false)
const approvalForm = reactive({
  id: '',
  repairApprovalStatus: 0,
  repairApprovalComment: '',
})
const approvalLoading = ref(false)
const approvalDialogError = ref('')
const detailReportPhotos = ref([])
const detailRepairPhotos = ref([])
const showImageConfirmDialog = ref(false)
const deletingImage = ref(null)
const deletingImageSection = ref('')

const openDetailDialog = async (record) => {
  detailRecord.value = record
  showDetailDialog.value = true
  detailReportPhotos.value = []
  detailRepairPhotos.value = []
  try {
    const [legacyRes, repairRes] = await Promise.all([
      fetchBizFiles('mold', record.moldId, 'repair_photo'),
      fetchBizFiles('repair', record.id, 'repair_photo'),
    ])
    const legacy = legacyRes.data || []
    const repairFiles = repairRes.data || []
    detailReportPhotos.value = [
      ...legacy,
      ...repairFiles.filter(
        (f) => !f.imageStatus || f.imageStatus === 'REPORT'
      ),
    ]
    detailRepairPhotos.value = repairFiles.filter(
      (f) => f.imageStatus === 'FINISH'
    )
  } catch (e) {
    console.error(e)
  }
}

const closeDetailDialog = () => {
  showDetailDialog.value = false
}

const openApprovalDialog = (record) => {
  approvalForm.id = record.id
  approvalForm.repairApprovalStatus = record.repairApprovalStatus ?? 0
  const rawComment = record.repairApprovalComment
  approvalForm.repairApprovalComment = (rawComment == null || rawComment === 'null' || String(rawComment).trim() === '') ? '' : String(rawComment)
  approvalDialogError.value = ''
  showApprovalDialog.value = true
}

const closeApprovalDialog = () => {
  showApprovalDialog.value = false
  approvalForm.id = ''
  approvalForm.repairApprovalStatus = 0
  approvalForm.repairApprovalComment = ''
  approvalDialogError.value = ''
}

const handleApprovalSubmit = async () => {
  if (!approvalForm.id) return
  approvalDialogError.value = ''
  approvalLoading.value = true
  try {
    await approveRepairRecord(approvalForm.id, {
      status: approvalForm.repairApprovalStatus ?? 0,
      comment: approvalForm.repairApprovalComment || null,
    })
    successMessage.value = '审批已提交'
    showApprovalDialog.value = false
    await loadRecords()
    setTimeout(() => { successMessage.value = '' }, 3000)
  } catch (e) {
    approvalDialogError.value = e.message || '审批提交失败'
  } finally {
    approvalLoading.value = false
  }
}

const handlePreviewFile = async (file) => {
  if (!file || !file.id) return
  try {
    const res = await getFilePreviewUrl(file.id)
    const url = res.data
    if (url) {
      window.open(url, '_blank')
    }
  } catch (e) {
    console.error(e)
  }
}

const previewSelectedFile = (file) => {
  if (!file) return
  const url = URL.createObjectURL(file)
  window.open(url, '_blank')
  setTimeout(() => URL.revokeObjectURL(url), 60_000)
}

const handleDeleteDetailFile = (file, section) => {
  if (!file || !file.id) return
  deletingImage.value = file
  deletingImageSection.value = section
  showImageConfirmDialog.value = true
}

const confirmDeleteDetailFile = async () => {
  const file = deletingImage.value
  const section = deletingImageSection.value
  if (!file || !file.id) {
    showImageConfirmDialog.value = false
    return
  }
  try {
    await deleteFiles([file.id])
    if (section === 'REPORT') {
      detailReportPhotos.value = detailReportPhotos.value.filter(
        (f) => f.id !== file.id
      )
    } else if (section === 'FINISH') {
      detailRepairPhotos.value = detailRepairPhotos.value.filter(
        (f) => f.id !== file.id
      )
    }
    showImageConfirmDialog.value = false
  } catch (e) {
    console.error(e)
  }
}

const handleAcceptAndApply = async () => {
  acceptDialogErrorMessage.value = ''
  if (!acceptForm.id || !acceptForm.moldId) {
    acceptDialogErrorMessage.value = '记录ID或模具ID缺失，无法验收'
    return
  }
  if (acceptForm.decision !== 'pass') {
    acceptDialogErrorMessage.value = '仅“通过”时才能完成验收，请先选择通过或点击“仅保存”'
    return
  }
  acceptLoading.value = true
  try {
    await ensureUserListLoaded()
    if (!currentUserId.value) {
      throw new Error('无法获取当前用户ID，请联系管理员配置用户信息')
    }
    const payload = {
      id: acceptForm.id,
      moldId: acceptForm.moldId,
      verifierId: currentUserId.value,
      status: 4,
      notes: acceptForm.comment || null,
    }
    await updateRepairRecord(payload)
    successMessage.value = '验收通过'
    showAcceptDialog.value = false
    await loadRecords()
    setTimeout(() => {
      successMessage.value = ''
    }, 3000)
  } catch (e) {
    acceptDialogErrorMessage.value = e.message || '提交验收结果失败'
  } finally {
    acceptLoading.value = false
  }
}

onMounted(async () => {
  if (!authStore.isAuthenticated) {
    router.push('/login')
    return
  }
  // 从模具页跳转过来时带 moldId，仅显示该模具的维修记录（无记录则列表为空）
  if (route.query.moldId) {
    query.moldId = String(route.query.moldId)
  }
  await Promise.all([loadMoldOptions(), loadUserOptions(), loadRecords()])
})
</script>

<style scoped>
.repair-records-container {
  min-height: 100vh;
  background: #f3f4f6;
  display: flex;
  flex-direction: row;
}

.sidebar {
  width: 220px;
  background: #1e3c72;
  color: #e5e7eb;
  display: flex;
  flex-direction: column;
  padding: 16px 12px;
}

.sidebar-header {
  padding: 12px 8px 20px;
}

.sidebar-logo {
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 1px;
}

.sidebar-menu {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 6px;
  cursor: pointer;
  color: #e5e7eb;
  font-size: 14px;
  transition: background 0.2s, color 0.2s;
}

.parent-item {
  font-weight: 600;
  justify-content: flex-start;
}

.child-item {
  padding-left: 28px;
  font-size: 13px;
}

.submenu-arrow {
  font-size: 12px;
}

.menu-item .menu-icon {
  width: 18px;
  text-align: center;
}

.menu-item.active {
  background: #2563eb;
}

.menu-item:not(.active):hover {
  background: rgba(148, 163, 184, 0.25);
}

.menu-item.disabled {
  opacity: 0.6;
  cursor: default;
}

.sidebar-footer {
  padding-top: 12px;
  border-top: 1px solid rgba(148, 163, 184, 0.4);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.sidebar-username {
  font-size: 13px;
}

.sidebar-logout {
  padding: 6px 10px;
  background: transparent;
  color: #e5e7eb;
  border-radius: 4px;
  border: 1px solid rgba(148, 163, 184, 0.8);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.sidebar-logout:hover {
  background: rgba(148, 163, 184, 0.3);
}

.layout-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.top-header {
  padding: 16px 24px 8px;
}

.top-title {
  font-size: 22px;
  font-weight: 600;
  color: #111827;
}

.top-subtitle {
  font-size: 13px;
  color: #6b7280;
  margin-top: 4px;
}

.main-content {
  flex: 1;
  padding: 24px;
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
}

.content-wrapper {
  width: 100%;
}

.card {
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.card-header {
  padding: 20px 24px;
  border-bottom: 1px solid #e5e7eb;
  background: #f9fafb;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.card-body {
  padding: 24px;
}

.query-form {
  margin-bottom: 20px;
  padding: 16px;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.query-row {
  display: flex;
  flex-wrap: wrap;
  gap: 16px 20px;
  align-items: flex-end;
}

.query-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 140px;
  flex: 1;
  max-width: 220px;
}

.query-item label {
  font-size: 13px;
  color: #374151;
  font-weight: 500;
}

.query-input {
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
}

.query-input:focus {
  outline: none;
  border-color: #2a5298;
  box-shadow: 0 0 0 2px rgba(42, 82, 152, 0.15);
}

.query-actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}

.pagination {
  margin-top: 16px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.page-btn {
  padding: 6px 12px;
  border-radius: 4px;
  border: 1px solid #d1d5db;
  background: #fff;
  cursor: pointer;
  font-size: 13px;
}

.page-btn:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.page-btn:hover:not(:disabled) {
  background: #f3f4f6;
}

.page-info {
  font-size: 13px;
  color: #6b7280;
}

.mold-filter-hint {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  padding: 10px 14px;
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: 6px;
  font-size: 13px;
  color: #1e40af;
}

.mold-filter-hint .link-btn {
  padding: 0;
  background: none;
  border: none;
  color: #2563eb;
  cursor: pointer;
  font-size: 13px;
  text-decoration: underline;
}

.mold-filter-hint .link-btn:hover {
  color: #1d4ed8;
}

.primary-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  color: #ffffff;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 6px rgba(30, 60, 114, 0.2);
}

.primary-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(30, 60, 114, 0.3);
}

.secondary-btn {
  padding: 10px 20px;
  background: #ffffff;
  color: #374151;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.secondary-btn:hover {
  background: #f3f4f6;
  border-color: #9ca3af;
}

.success-message,
.error-message {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 14px;
  padding: 12px 16px;
  border-radius: 6px;
  margin-bottom: 16px;
}

.success-message {
  color: #065f46;
  background: #d1fae5;
  border: 1px solid #a7f3d0;
}

.error-message {
  color: #991b1b;
  background: #fee2e2;
  border: 1px solid #fecaca;
}

.message-icon {
  font-size: 16px;
  font-weight: bold;
}

.table-loading {
  text-align: center;
  padding: 20px 0;
  color: #6b7280;
  font-size: 14px;
}

.table-wrapper {
  width: 100%;
  overflow-x: auto;
}

.mold-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.mold-table th,
.mold-table td {
  padding: 8px 10px;
  border-bottom: 1px solid #e5e7eb;
  text-align: left;
}

.mold-table th {
  background: #f9fafb;
  font-weight: 600;
  color: #374151;
}

.mold-table tbody tr:hover {
  background: #f3f4f6;
}

.empty-cell {
  text-align: center;
  color: #9ca3af;
}

.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.action-btn {
  padding: 4px 10px;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.edit-btn {
  background: #3b82f6;
  color: #ffffff;
}

.edit-btn:hover {
  background: #2563eb;
}

.status-btn {
  background: #e5e7eb;
  color: #111827;
}

.status-btn:hover {
  background: #d1d5db;
}

.delete-btn {
  background: #ef4444;
  color: #ffffff;
}

.delete-btn:hover {
  background: #dc2626;
}

.action-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.status-normal {
  color: #10b981;
  font-weight: 500;
}

.status-warning {
  color: #f59e0b;
  font-weight: 500;
}

.status-danger {
  color: #ef4444;
  font-weight: 500;
}

.dialog-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.dialog-content {
  background: #ffffff;
  border-radius: 12px;
  width: 90%;
  max-width: 800px;
  max-height: 90vh;
  overflow: hidden;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  display: flex;
  flex-direction: column;
}

.dialog-content-sm {
  max-width: 420px;
}

.dialog-header {
  padding: 20px 24px;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
}

.dialog-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #ffffff;
}

.dialog-close {
  background: rgba(255, 255, 255, 0.15);
  border: none;
  font-size: 20px;
  color: #ffffff;
  cursor: pointer;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
}

.dialog-body {
  padding: 20px 24px;
  overflow-y: auto;
}

.mold-info-text {
  font-size: 13px;
  color: #4b5563;
  margin: 4px 0 8px;
}

.mold-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-section-title {
  font-size: 14px;
  font-weight: 600;
  color: #4b5563;
  margin-top: 4px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group label {
  font-size: 13px;
  color: #374151;
}

.form-input {
  padding: 8px 10px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  background: #ffffff;
  transition: all 0.2s;
}

.form-input:focus {
  outline: none;
  border-color: #1e3c72;
  box-shadow: 0 0 0 3px rgba(30, 60, 114, 0.1);
}

.readonly-text {
  background: #f9fafb;
  cursor: default;
}

.readonly-text.multiline {
  min-height: 60px;
  display: flex;
  align-items: flex-start;
}

.selected-files {
  margin-top: 4px;
  font-size: 12px;
  color: #4b5563;
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.selected-file-item {
  background: #e5e7eb;
  border-radius: 999px;
  padding: 2px 8px;
}

.no-files {
  font-size: 13px;
  color: #9ca3af;
}

.file-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.file-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.file-main {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.file-name {
  font-size: 14px;
  color: #111827;
}

.file-meta {
  font-size: 12px;
  color: #6b7280;
}

.file-preview-btn {
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid #d1d5db;
  background: #ffffff;
  font-size: 12px;
  cursor: pointer;
  white-space: nowrap;
  color: #1d4ed8;
  transition: all 0.2s;
}

.file-preview-btn:hover {
  background: #eff6ff;
  border-color: #93c5fd;
}

.file-delete-btn {
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid #fecaca;
  background: #fef2f2;
  font-size: 12px;
  cursor: pointer;
  white-space: nowrap;
  color: #b91c1c;
  transition: all 0.2s;
}

.file-delete-btn:hover {
  background: #fee2e2;
}

.dialog-actions {
  display: flex;
  gap: 12px;
  margin-top: 12px;
}

.submit-button {
  flex: 1;
  padding: 10px 16px;
  background: #1e3c72;
  color: #ffffff;
  border-radius: 6px;
  border: none;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
}

.submit-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.cancel-button {
  flex: 1;
  padding: 10px 16px;
  background: #ffffff;
  color: #374151;
  border-radius: 6px;
  border: 1px solid #d1d5db;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
}

.cancel-button:hover {
  background: #f9fafb;
}

/* 图片删除确认对话框样式 */
.confirm-dialog {
  background: #ffffff;
  border-radius: 16px;
  width: 90%;
  max-width: 420px;
  padding: 32px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  text-align: center;
}

.confirm-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 20px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
}

.confirm-icon.danger {
  background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
}

.confirm-title {
  margin: 0 0 12px;
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

.confirm-message {
  margin: 0 0 28px;
  font-size: 15px;
  color: #6b7280;
}

.confirm-actions {
  display: flex;
  gap: 12px;
}

.confirm-cancel-btn,
.confirm-ok-btn {
  flex: 1;
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.confirm-cancel-btn {
  background: #f3f4f6;
  color: #374151;
}

.confirm-ok-btn.danger {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  color: #ffffff;
}

@media (max-width: 768px) {
  .repair-records-container {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
  }

  .sidebar-menu {
    flex-direction: row;
  }

  .main-content {
    padding: 16px;
  }

  .form-row {
    grid-template-columns: 1fr;
  }

  .dialog-content {
    width: 95%;
    max-width: none;
  }
}
</style>

