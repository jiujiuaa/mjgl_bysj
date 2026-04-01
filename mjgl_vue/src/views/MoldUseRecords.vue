<template>
  <div class="mold-use-records-container">
    <!-- 全局左侧边栏 -->
    <AppSidebar />

    <!-- 右侧主区域 -->
    <div class="layout-main" v-back-to-top>
      <header class="top-header">
        <div class="top-title">模具使用记录</div>
        <div class="top-subtitle">查看与维护模具使用/借出记录</div>
      </header>

      <main class="main-content">
        <div class="content-wrapper">
          <section>
            <div class="card">
              <div class="card-header">
                <h3 class="card-title">使用记录列表</h3>
                <div class="card-header-actions">
                  <button
                    type="button"
                    class="secondary-btn delete-outline-btn"
                    :disabled="useBatchDeleting || selectedIds.length === 0"
                    @click="handleBatchDeleteRecords"
                  >
                    {{ useBatchDeleting ? '删除中...' : `批量删除 (${selectedIds.length})` }}
                  </button>
                  <button
                    class="primary-btn"
                    type="button"
                    @click="handleShowCreateDialog"
                  >
                    新建记录
                  </button>
                </div>
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

                <!-- 查询条件：模具编号 + 状态 + 使用类型（模具ID 仅通过路由/内部使用，不在界面展示） -->
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
                        v-model="query.moldKeyword"
                        type="text"
                        class="form-input query-input"
                        placeholder="支持名称或编号模糊匹配"
                      />
                    </div>
                    <div class="query-item">
                      <label>状态</label>
                      <select v-model="query.status" class="form-input query-input">
                        <option :value="null">全部</option>
                        <option :value="1">在库</option>
                        <option :value="2">使用中</option>
                        <option :value="3">使用完成</option>
                      </select>
                    </div>
                    <div class="query-item">
                      <label>使用类型</label>
                      <select v-model="query.usageType" class="form-input query-input">
                        <option :value="null">全部</option>
                        <option :value="1">内部生产</option>
                        <option :value="2">外借</option>
                        <option :value="3">试模</option>
                      </select>
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

                <div v-if="listLoading" class="table-loading">使用记录加载中...</div>
                <div v-else>
                  <div class="table-wrapper">
                    <table class="mold-table">
                      <thead>
                        <tr>
                          <th class="select-col">
                            <input
                              type="checkbox"
                              :checked="isAllPageSelected()"
                              @change="toggleSelectAllPage($event.target.checked)"
                              title="全选本页（当前筛选结果）"
                            />
                          </th>
                          <th>模具编号</th>
                          <th>模具名称</th>
                          <th>类别</th>
                          <th>使用类型</th>
                          <th>预计开始</th>
                          <th>预计结束</th>
                          <th>计划时长(h)</th>
                          <th>实际开始</th>
                          <th>实际结束</th>
                          <th>实际时长(h)</th>
                          <th>申请人</th>
                          <th>借用人</th>
                          <th>借用单位</th>
                          <th>状态</th>
                          <th>归还验收</th>
                          <th>合理性审批</th>
                          <th>创建时间</th>
                          <th>操作</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-if="filteredRecords.length === 0">
                          <td colspan="19" class="empty-cell">暂无使用记录</td>
                        </tr>
                        <tr
                          v-for="record in filteredRecords"
                          :key="record.id"
                        >
                          <td class="select-col">
                            <input
                              type="checkbox"
                              :checked="isSelected(record.id)"
                              @change="toggleRow(record.id)"
                            />
                          </td>
                          <td>{{ record.moldCode }}</td>
                          <td>{{ record.moldName }}</td>
                          <td>{{ record.moldCategory || '-' }}</td>
                          <td>{{ record.usageTypeDesc || formatUsageType(record.usageType) }}</td>
                          <td>{{ formatDate(record.scheduledStartTime) }}</td>
                          <td>{{ formatDate(record.scheduledEndTime) }}</td>
                          <td>{{ record.plannedDurationHours ?? '-' }}</td>
                          <td>{{ formatDate(record.actualStartTime) }}</td>
                          <td>{{ formatDate(record.actualEndTime) }}</td>
                          <td>{{ record.actualDurationHours ?? '-' }}</td>
                          <td>{{ record.applicantName || '-' }}</td>
                          <td>{{ record.borrowerName || '-' }}</td>
                          <td>{{ record.borrowerCompany || '-' }}</td>
                          <td>
                            <span :class="getStatusClass(record.status)">
                              {{ record.statusDesc || formatStatus(record.status) }}
                            </span>
                          </td>
                          <td>
                            <span v-if="record.inspectionPassed === true" class="status-normal">
                              通过
                            </span>
                            <span v-else-if="record.inspectionPassed === false" class="status-danger">
                              未通过
                            </span>
                            <span v-else>-</span>
                          </td>
                          <td>
                            <span
                              v-if="record.usageApprovalStatus === 1"
                              class="status-normal"
                            >
                              合理
                            </span>
                            <span
                              v-else-if="record.usageApprovalStatus === 2"
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
                                class="action-btn edit-btn"
                                @click="openDetailDialog(record)"
                              >
                                详情
                              </button>
                              <button
                                class="action-btn edit-btn"
                                @click="handleEdit(record)"
                              >
                                编辑
                              </button>
                              <button
                                v-if="record.status === 1"
                                class="action-btn status-btn"
                                :disabled="rowLoadingId === record.id"
                                @click="handleChangeStatus(record, 2)"
                              >
                                开始使用
                              </button>
                              <button
                                v-else-if="record.status === 2"
                                class="action-btn status-btn"
                                :disabled="rowLoadingId === record.id"
                                @click="handleEdit(record, true)"
                              >
                                结束使用
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
                </div>
              </div>
            </div>
          </section>
        </div>
      </main>
    </div>

    <!-- 编辑使用记录对话框（主要维护实际时间 + 验收结果） -->
    <div v-if="showEditDialog" class="dialog-overlay" @click="closeEditDialog">
      <div class="dialog-content" @click.stop>
        <div class="dialog-header">
          <h3>编辑使用记录</h3>
          <button class="dialog-close" @click="closeEditDialog">×</button>
        </div>
        <div class="dialog-body">
          <form class="mold-form" @submit.prevent="handleSave">
            <div class="form-section-title">基本信息</div>
            <p class="mold-info-text">
              模具：{{ editForm.moldName || '-' }} / {{ editForm.moldCode || '-' }} /
              {{ editForm.moldCategory || '-' }}
            </p>
            <div class="form-row">
              <div class="form-group">
                <label>模具编号</label>
                <input v-model="editForm.moldCode" class="form-input" disabled />
              </div>
              <div class="form-group">
                <label>模具名称</label>
                <input v-model="editForm.moldName" class="form-input" disabled />
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>使用类型</label>
                <input
                  :value="formatUsageType(editForm.usageType)"
                  class="form-input"
                  disabled
                />
              </div>
            </div>

            <div class="form-section-title">预计时间</div>
            <div class="form-row">
              <div class="form-group">
                <label>预计开始时间</label>
                <input
                  type="text"
                  class="form-input"
                  :value="formatDate(editForm.scheduledStartTime)"
                  disabled
                />
              </div>
              <div class="form-group">
                <label>预计结束时间</label>
                <input
                  type="text"
                  class="form-input"
                  :value="formatDate(editForm.scheduledEndTime)"
                  disabled
                />
              </div>
            </div>

            <div class="form-section-title">实际时间</div>
            <div class="form-row">
              <div class="form-group">
                <label for="actualStart">实际开始时间</label>
                <HourDateTimePicker v-model="editForm.actualStartTime" />
              </div>
              <div class="form-group">
                <label for="actualEnd">实际结束时间</label>
                <HourDateTimePicker v-model="editForm.actualEndTime" />
              </div>
            </div>

            <div
              v-if="!isEndUseMode && shouldShowBorrowerFields"
              class="form-section-title"
            >
              借用与归还信息
            </div>
            <div v-if="!isEndUseMode && shouldShowBorrowerFields" class="form-row">
              <div class="form-group">
                <label for="borrowerName">借用人</label>
                <input
                  id="borrowerName"
                  v-model="editForm.borrowerName"
                  type="text"
                  class="form-input"
                />
              </div>
              <div class="form-group">
                <label for="borrowerCompany">借用单位</label>
                <input
                  id="borrowerCompany"
                  v-model="editForm.borrowerCompany"
                  type="text"
                  class="form-input"
                />
              </div>
            </div>

            <div class="form-group">
              <label for="purpose">用途说明</label>
              <input
                id="purpose"
                v-model="editForm.purpose"
                type="text"
                class="form-input"
              />
            </div>

            <div class="form-row">
              <div class="form-group">
                <label for="inspectionPassed">归还验收</label>
                <select
                  id="inspectionPassed"
                  v-model="editForm.inspectionPassed"
                  class="form-input"
                >
                  <option :value="null">未设置</option>
                  <option :value="true">通过</option>
                  <option :value="false">未通过</option>
                </select>
              </div>
              <div class="form-group">
                <label for="returnRemarks">归还备注</label>
                <input
                  id="returnRemarks"
                  v-model="editForm.returnRemarks"
                  type="text"
                  class="form-input"
                />
              </div>
            </div>

            <div v-if="dialogErrorMessage" class="error-message">
              <span class="message-icon">⚠</span>
              {{ dialogErrorMessage }}
            </div>

            <div class="dialog-actions">
              <button
                type="submit"
                class="submit-button"
                :disabled="saveLoading"
              >
                {{ saveLoading ? '保存中...' : '保存' }}
              </button>
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

    <!-- 使用记录详情对话框（只读） -->
    <div v-if="showDetailDialog" class="dialog-overlay" @click="closeDetailDialog">
      <div class="dialog-content" @click.stop>
        <div class="dialog-header">
          <h3>使用记录详情</h3>
          <button class="dialog-close" @click="closeDetailDialog">×</button>
        </div>
        <div class="dialog-body" v-if="detailRecord">
          <div class="form-section-title">基本信息</div>
          <p class="mold-info-text">
            模具：{{ detailRecord.moldName || '-' }} / {{ detailRecord.moldCode || '-' }} /
            {{ detailRecord.moldCategory || '-' }}
          </p>
          <div class="form-row">
            <div class="form-group">
              <label>使用类型</label>
              <div class="form-input readonly-text">
                {{ detailRecord.usageTypeDesc || formatUsageType(detailRecord.usageType) }}
              </div>
            </div>
            <div class="form-group">
              <label>状态</label>
              <div class="form-input readonly-text">
                {{ detailRecord.statusDesc || formatStatus(detailRecord.status) }}
              </div>
            </div>
          </div>
          <div class="form-section-title">时间信息</div>
          <div class="form-row">
            <div class="form-group">
              <label>预计开始</label>
              <div class="form-input readonly-text">{{ formatDate(detailRecord.scheduledStartTime) }}</div>
            </div>
            <div class="form-group">
              <label>预计结束</label>
              <div class="form-input readonly-text">{{ formatDate(detailRecord.scheduledEndTime) }}</div>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>实际开始</label>
              <div class="form-input readonly-text">{{ formatDate(detailRecord.actualStartTime) }}</div>
            </div>
            <div class="form-group">
              <label>实际结束</label>
              <div class="form-input readonly-text">{{ formatDate(detailRecord.actualEndTime) }}</div>
            </div>
          </div>
          <div class="form-section-title">借用与归还</div>
          <div class="form-row">
            <div class="form-group">
              <label>申请人</label>
              <div class="form-input readonly-text">{{ detailRecord.applicantName || '-' }}</div>
            </div>
            <div class="form-group">
              <label>借用人</label>
              <div class="form-input readonly-text">{{ detailRecord.borrowerName || '-' }}</div>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>借用单位</label>
              <div class="form-input readonly-text">{{ detailRecord.borrowerCompany || '-' }}</div>
            </div>
            <div class="form-group">
              <label>用途说明</label>
              <div class="form-input readonly-text">{{ detailRecord.purpose || '-' }}</div>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>归还验收</label>
              <div class="form-input readonly-text">
                <span v-if="detailRecord.inspectionPassed === true" class="status-normal">通过</span>
                <span v-else-if="detailRecord.inspectionPassed === false" class="status-danger">未通过</span>
                <span v-else>-</span>
              </div>
            </div>
            <div class="form-group">
              <label>归还备注</label>
              <div class="form-input readonly-text">{{ detailRecord.returnRemarks || '-' }}</div>
            </div>
          </div>
          <div class="form-section-title">合理性审批</div>
          <div class="form-row">
            <div class="form-group">
              <label>审批结果</label>
              <div class="form-input readonly-text">
                {{ formatApprovalStatus(detailRecord.usageApprovalStatus) }}
              </div>
            </div>
            <div class="form-group">
              <label>审批意见</label>
              <div class="form-input readonly-text">{{ detailRecord.usageApprovalComment || '-' }}</div>
            </div>
          </div>

          <div class="form-section-title">时间线</div>
          <div class="form-row">
            <div class="form-group">
              <label>申请</label>
              <div class="form-input readonly-text">
                {{ detailRecord.applicantName || '-' }} / {{ formatDate(detailRecord.createdAt) }}
              </div>
            </div>
            <div class="form-group">
              <label>申请原因</label>
              <div class="form-input readonly-text">{{ detailRecord.purpose || '-' }}</div>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>领用/开始</label>
              <div class="form-input readonly-text">{{ formatDate(detailRecord.actualStartTime) }}</div>
            </div>
            <div class="form-group">
              <label>借用人</label>
              <div class="form-input readonly-text">{{ detailRecord.borrowerName || '-' }}</div>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>归还/验收</label>
              <div class="form-input readonly-text">
                {{ formatDate(detailRecord.actualEndTime) }} /
                <span v-if="detailRecord.inspectionPassed === true" class="status-normal">通过</span>
                <span v-else-if="detailRecord.inspectionPassed === false" class="status-danger">未通过</span>
                <span v-else>-</span>
              </div>
            </div>
            <div class="form-group">
              <label>归还备注</label>
              <div class="form-input readonly-text">{{ detailRecord.returnRemarks || '-' }}</div>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>审批/处理人</label>
              <div class="form-input readonly-text">
                {{ detailRecord.usageApproverName || '-' }} / {{ formatDate(detailRecord.usageApprovalTime) }}
              </div>
            </div>
            <div class="form-group">
              <label>审批意见</label>
              <div class="form-input readonly-text">{{ detailRecord.usageApprovalComment || '-' }}</div>
            </div>
          </div>
          <div class="dialog-actions">
            <button type="button" class="cancel-button" @click="closeDetailDialog">关闭</button>
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
                v-model.number="approvalForm.usageApprovalStatus"
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
                v-model="approvalForm.usageApprovalComment"
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

    <!-- 新建使用记录对话框 -->
    <div
      v-if="showCreateDialog"
      class="dialog-overlay"
      @click="closeCreateDialog"
    >
      <div class="dialog-content" @click.stop>
        <div class="dialog-header">
          <h3>新建使用记录</h3>
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

            <div class="form-row">
              <div class="form-group">
                <label for="createUsageType">使用类型 *</label>
                <select
                  id="createUsageType"
                  v-model.number="createForm.usageType"
                  class="form-input"
                  required
                >
                  <option :value="null">请选择</option>
                  <option :value="1">内部生产</option>
                  <option :value="2">外借</option>
                  <option :value="3">试模</option>
                </select>
              </div>
            </div>

            <div class="form-section-title">预计时间</div>
            <div class="form-row">
              <div class="form-group">
                <label for="createScheduledStart">预计开始时间</label>
                <HourDateTimePicker v-model="createForm.scheduledStartTime" />
              </div>
              <div class="form-group">
                <label for="createScheduledEnd">预计结束时间</label>
                <HourDateTimePicker v-model="createForm.scheduledEndTime" />
              </div>
            </div>

            <div
              v-if="createForm.usageType !== 1"
              class="form-section-title"
            >
              借用信息（仅外借等场景需要）
            </div>
            <div
              v-if="createForm.usageType !== 1"
              class="form-row"
            >
              <div class="form-group">
                <label for="createBorrowerName">借用人</label>
                <input
                  id="createBorrowerName"
                  v-model="createForm.borrowerName"
                  type="text"
                  class="form-input"
                />
              </div>
              <div class="form-group">
                <label for="createBorrowerCompany">借用单位</label>
                <input
                  id="createBorrowerCompany"
                  v-model="createForm.borrowerCompany"
                  type="text"
                  class="form-input"
                />
              </div>
            </div>

            <div class="form-group">
              <label for="createPurpose">用途说明</label>
              <input
                id="createPurpose"
                v-model="createForm.purpose"
                type="text"
                class="form-input"
              />
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
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import AppSidebar from '@/components/AppSidebar.vue'
import HourDateTimePicker from '@/components/HourDateTimePicker.vue'
import { useTableMultiSelect } from '@/composables/useTableMultiSelect'
import {
  fetchAllUseRecords,
  fetchUseRecordsByMoldId,
  fetchUseRecordById,
  updateUseRecord,
  createUseRecord,
  updateUseRecordStatus,
  deleteUseRecord,
  batchDeleteUseRecords,
  approveUseRecord,
} from '@/api/useRecords'
import { fetchMolds } from '@/api/molds'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const showOpsChildren = ref(true)
const showMonitoringChildren = ref(true)

const listLoading = ref(false)
const successMessage = ref('')
const errorMessage = ref('')

const records = ref([])
const rowLoadingId = ref('')

const moldOptions = ref([])
const moldOptionsLoaded = ref(false)
const moldOptionsLoading = ref(false)

const query = reactive({
  moldKeyword: '',
  moldId: '',
  status: null,
  usageType: null,
})

const showEditDialog = ref(false)
const saveLoading = ref(false)
const dialogErrorMessage = ref('')
const isEndUseMode = ref(false)

const showCreateDialog = ref(false)
const createLoading = ref(false)
const createDialogErrorMessage = ref('')

const showDetailDialog = ref(false)
const detailRecord = ref(null)

const showApprovalDialog = ref(false)
const approvalForm = reactive({
  id: '',
  usageApprovalStatus: 0,
  usageApprovalComment: '',
})
const approvalLoading = ref(false)
const approvalDialogError = ref('')

const editForm = reactive({
  id: '',
  moldId: '',
  usageType: null,
  moldCode: '',
  moldName: '',
  moldCategory: '',
  scheduledStartTime: '',
  scheduledEndTime: '',
  actualStartTime: '',
  actualEndTime: '',
  borrowerName: '',
  borrowerCompany: '',
  purpose: '',
  inspectionPassed: null,
  returnRemarks: '',
})

const createForm = reactive({
  moldId: '',
  moldCode: '',
  moldName: '',
  moldCategory: '',
  usageType: null,
  scheduledStartTime: '',
  scheduledEndTime: '',
  borrowerName: '',
  borrowerCompany: '',
  purpose: '',
})

const isManager = computed(() => {
  const role = authStore.role
  return role === 'ADMIN' || role === 'INSPECTOR'
})

const isAdmin = computed(() => authStore.role === 'ADMIN')
const shouldShowBorrowerFields = computed(() => Number(editForm.usageType) === 2)

const filteredRecords = computed(() => {
  return records.value.filter((r) => {
    const keyword = query.moldKeyword?.trim()
    if (
      keyword &&
      !(
        String(r.moldCode || '').includes(keyword) ||
        String(r.moldName || '').includes(keyword)
      )
    ) {
      return false
    }
    if (query.status != null && r.status !== query.status) {
      return false
    }
    if (query.usageType != null && r.usageType !== query.usageType) {
      return false
    }
    return true
  })
})

const useBatchDeleting = ref(false)
const { selectedIds, isSelected, toggleRow, isAllPageSelected, toggleSelectAllPage, clearSelection } =
  useTableMultiSelect(() => filteredRecords.value)

const loadRecords = async () => {
  listLoading.value = true
  errorMessage.value = ''
  try {
    let res
    if (query.moldId && query.moldId.trim()) {
      res = await fetchUseRecordsByMoldId(query.moldId.trim())
    } else {
      res = await fetchAllUseRecords()
    }
    records.value = res.data || []
  } catch (e) {
    errorMessage.value = e.message || '加载使用记录失败'
  } finally {
    listLoading.value = false
  }
}

const handleQuery = () => {
  loadRecords()
}

const handleReset = () => {
  query.moldKeyword = ''
  query.moldId = ''
  query.status = null
  query.usageType = null
  loadRecords()
}

const loadMoldOptions = async () => {
  if (moldOptionsLoaded.value || moldOptionsLoading.value) return
  moldOptionsLoading.value = true
  try {
    const res = await fetchMolds(1, 1000)
    moldOptions.value = res.data?.list || []
    moldOptionsLoaded.value = true
  } catch (e) {
    // 静默失败，保持列表为空
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
  // 如果从模具管理跳转过来，默认带上该模具 ID
  if (route.query.moldId) {
    createForm.moldId = String(route.query.moldId)
  } else if (!createForm.moldId && moldOptions.value.length === 1) {
    // 如果只有一个模具，默认选中
    createForm.moldId = moldOptions.value[0].id
  }
  syncCreateFormFromSelectedMold()
  createForm.usageType = null
  createForm.scheduledStartTime = ''
  createForm.scheduledEndTime = ''
  createForm.borrowerName = ''
  createForm.borrowerCompany = ''
  createForm.purpose = ''
  createDialogErrorMessage.value = ''
  showCreateDialog.value = true
}

const handleCreateMoldChange = () => {
  syncCreateFormFromSelectedMold()
}

const closeCreateDialog = () => {
  showCreateDialog.value = false
}

const handleEdit = async (record, endUse = false) => {
  dialogErrorMessage.value = ''
  isEndUseMode.value = endUse
  try {
    // 详情接口返回实体，保证拿到 moldId 等关键字段
    const res = await fetchUseRecordById(record.id)
    const detail = res.data || {}

    editForm.id = record.id
    editForm.moldId = detail.moldId || record.moldId || ''
    editForm.usageType = detail.usageType ?? record.usageType ?? null
    editForm.moldCode = record.moldCode
    editForm.moldName = record.moldName
    // moldCategory 由后端 VO 提供
    editForm.moldCategory = record.moldCategory || ''
    editForm.scheduledStartTime = record.scheduledStartTime
    editForm.scheduledEndTime = record.scheduledEndTime
    editForm.actualStartTime = detail.actualStartTime || record.actualStartTime || ''
    editForm.actualEndTime = detail.actualEndTime || record.actualEndTime || ''
    editForm.borrowerName = detail.borrowerName || record.borrowerName || ''
    editForm.borrowerCompany = detail.borrowerCompany || record.borrowerCompany || ''
    editForm.purpose = detail.purpose || record.purpose || ''
    const passed =
      detail.returnInspectionPassed ??
      detail.inspectionPassed ??
      record.inspectionPassed
    editForm.inspectionPassed =
      passed === null || passed === undefined ? null : !!passed
    editForm.returnRemarks = detail.returnRemarks || record.returnRemarks || ''

    showEditDialog.value = true
  } catch (e) {
    errorMessage.value = e.message || '加载使用记录详情失败'
  }
}

const closeEditDialog = () => {
  showEditDialog.value = false
  isEndUseMode.value = false
}

const openDetailDialog = (record) => {
  detailRecord.value = { ...record }
  showDetailDialog.value = true
}

const closeDetailDialog = () => {
  showDetailDialog.value = false
  detailRecord.value = null
}

const openApprovalDialog = (record) => {
  approvalForm.id = record.id
  approvalForm.usageApprovalStatus = record.usageApprovalStatus ?? 0
  const rawComment = record.usageApprovalComment
  approvalForm.usageApprovalComment = (rawComment == null || rawComment === 'null' || String(rawComment).trim() === '') ? '' : String(rawComment)
  approvalDialogError.value = ''
  showApprovalDialog.value = true
}

const closeApprovalDialog = () => {
  showApprovalDialog.value = false
  approvalForm.id = ''
  approvalForm.usageApprovalStatus = 0
  approvalForm.usageApprovalComment = ''
  approvalDialogError.value = ''
}

const handleApprovalSubmit = async () => {
  if (!approvalForm.id) return
  approvalDialogError.value = ''
  approvalLoading.value = true
  try {
    await approveUseRecord(approvalForm.id, {
      status: approvalForm.usageApprovalStatus ?? 0,
      comment: approvalForm.usageApprovalComment || null,
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

const handleSave = async () => {
  dialogErrorMessage.value = ''

  if (!editForm.id || !editForm.moldId) {
    dialogErrorMessage.value = '记录ID或模具ID缺失，无法保存'
    return
  }

  const parse = (s) => (s ? new Date(s.replace(' ', 'T')) : null)

  const normalizeForBackend = (s) => {
    if (!s) return null
    const d = parse(s)
    if (!d || Number.isNaN(d.getTime())) {
      // 兜底：直接返回原字符串，交给后端处理
      return s
    }
    const pad = (n) => String(n).padStart(2, '0')
    const yyyy = d.getFullYear()
    const MM = pad(d.getMonth() + 1)
    const dd = pad(d.getDate())
    const HH = pad(d.getHours())
    const mm = pad(d.getMinutes())
    const ss = pad(d.getSeconds())
    // 后端 LocalDateTime 默认可以解析的格式：yyyy-MM-dd'T'HH:mm:ss
    return `${yyyy}-${MM}-${dd}T${HH}:${mm}:${ss}`
  }

  const rawStart = editForm.actualStartTime || null
  const rawEnd = editForm.actualEndTime || null

  if (rawStart && rawEnd && parse(rawEnd) < parse(rawStart)) {
    dialogErrorMessage.value = '实际结束时间不能早于实际开始时间'
    return
  }

  const payload = {
    id: editForm.id,
    moldId: editForm.moldId,
    actualStartTime: normalizeForBackend(rawStart),
    actualEndTime: normalizeForBackend(rawEnd),
    borrowerName:
      !isEndUseMode.value && shouldShowBorrowerFields.value ? editForm.borrowerName || null : null,
    borrowerCompany:
      !isEndUseMode.value && shouldShowBorrowerFields.value ? editForm.borrowerCompany || null : null,
    purpose: editForm.purpose || null,
    inspectionPassed:
      editForm.inspectionPassed === null ? null : !!editForm.inspectionPassed,
    returnRemarks: editForm.returnRemarks || null,
  }

  saveLoading.value = true
  try {
    await updateUseRecord(payload)
    if (isEndUseMode.value) {
      await updateUseRecordStatus(editForm.id, 3)
      successMessage.value = '结束使用并保存成功'
    } else {
      successMessage.value = '更新使用记录成功'
    }
    showEditDialog.value = false
    await loadRecords()
    setTimeout(() => {
      successMessage.value = ''
    }, 3000)
  } catch (e) {
    dialogErrorMessage.value = e.message || '更新使用记录失败'
  } finally {
    saveLoading.value = false
    isEndUseMode.value = false
  }
}

const handleCreate = async () => {
  createDialogErrorMessage.value = ''

  if (!createForm.moldId || !String(createForm.moldId).trim()) {
    createDialogErrorMessage.value = '模具ID不能为空'
    return
  }
  if (!createForm.usageType) {
    createDialogErrorMessage.value = '请选择使用类型'
    return
  }

  const parse = (s) => (s ? new Date(s.replace(' ', 'T')) : null)
  const scheduledStart = createForm.scheduledStartTime || null
  const scheduledEnd = createForm.scheduledEndTime || null

  if (scheduledStart && scheduledEnd && parse(scheduledEnd) < parse(scheduledStart)) {
    createDialogErrorMessage.value = '预计结束时间不能早于预计开始时间'
    return
  }

  const payload = {
    moldId: createForm.moldId.trim(),
    usageType: createForm.usageType,
    applicantName: authStore.username || null,
    scheduledStartTime: scheduledStart,
    scheduledEndTime: scheduledEnd,
    borrowerName:
      createForm.usageType === 1 ? null : createForm.borrowerName || null,
    borrowerCompany:
      createForm.usageType === 1 ? null : createForm.borrowerCompany || null,
    purpose: createForm.purpose || null,
  }

  createLoading.value = true
  try {
    await createUseRecord(payload)
    successMessage.value = '新建使用记录成功'
    showCreateDialog.value = false
    await loadRecords()
    setTimeout(() => {
      successMessage.value = ''
    }, 3000)
  } catch (e) {
    createDialogErrorMessage.value = e.message || '新建使用记录失败'
  } finally {
    createLoading.value = false
  }
}

const handleChangeStatus = async (record, status) => {
  if (!record || !record.id) return
  rowLoadingId.value = record.id
  try {
    await updateUseRecordStatus(record.id, status)
    successMessage.value = '更新状态成功'
    await loadRecords()
    setTimeout(() => {
      successMessage.value = ''
    }, 3000)
  } catch (e) {
    errorMessage.value = e.message || '更新状态失败'
  } finally {
    rowLoadingId.value = ''
  }
}

const handleDeleteRecord = async (record) => {
  if (!record || !record.id) return
  const ok = window.confirm(
    `确定要删除该使用记录吗？此操作不可恢复！\n模具：${record.moldCode || ''} ${record.moldName || ''}`
  )
  if (!ok) return
  rowLoadingId.value = record.id
  try {
    await deleteUseRecord(record.id)
    successMessage.value = '删除使用记录成功'
    clearSelection()
    await loadRecords()
    setTimeout(() => {
      successMessage.value = ''
    }, 3000)
  } catch (e) {
    errorMessage.value = e.message || '删除使用记录失败'
  } finally {
    rowLoadingId.value = ''
  }
}

const handleBatchDeleteRecords = async () => {
  const ids = [...selectedIds.value]
  if (!ids.length) return
  const ok = window.confirm(`确定批量删除选中的 ${ids.length} 条使用记录吗？此操作不可恢复！`)
  if (!ok) return
  useBatchDeleting.value = true
  errorMessage.value = ''
  try {
    await batchDeleteUseRecords(ids)
    successMessage.value = '批量删除成功'
    clearSelection()
    await loadRecords()
    setTimeout(() => {
      successMessage.value = ''
    }, 3000)
  } catch (e) {
    errorMessage.value = e.message || '批量删除失败'
  } finally {
    useBatchDeleting.value = false
  }
}

const formatUsageType = (type) => {
  if (!type) return '-'
  const map = {
    1: '内部生产',
    2: '外借',
    3: '试模',
  }
  return map[type] || type
}

const formatStatus = (status) => {
  if (status === null || status === undefined) return '-'
  const map = {
    1: '在库',
    2: '使用中',
    3: '使用完成',
  }
  return map[status] || status
}

const getStatusClass = (status) => {
  if (status === 2) return 'status-normal'
  if (status === 1) return 'status-warning'
  if (status === 3) return 'status-danger'
  return ''
}

const formatDate = (val) => {
  if (!val) return '-'
  const date = new Date(typeof val === 'string' ? val.replace(' ', 'T') : val)
  if (Number.isNaN(date.getTime())) return String(val)
  const pad = (n) => String(n).padStart(2, '0')
  const y = date.getFullYear()
  const M = pad(date.getMonth() + 1)
  const d = pad(date.getDate())
  const h = pad(date.getHours())
  const m = pad(date.getMinutes())
  return `${y}/${M}/${d} ${h}:${m}`
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

const goRepairRecords = () => {
  router.push('/repair-records')
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

const formatApprovalStatus = (val) => {
  if (val === 1) return '合理'
  if (val === 2) return '存在问题'
  return '未审核'
}

onMounted(() => {
  if (!authStore.isAuthenticated) {
    router.push('/login')
    return
  }
  // 如果从模具管理页跳转过来，优先使用路由 query 作为筛选条件
  if (route.query.moldId) {
    query.moldId = String(route.query.moldId)
  }
  if (route.query.moldCode) {
    query.moldKeyword = String(route.query.moldCode)
  }
  loadMoldOptions().then(() => loadRecords())
})
</script>

<style scoped>
.mold-use-records-container {
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

.card-header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.delete-outline-btn {
  color: #b91c1c;
  border-color: #fecaca;
}

.select-col {
  width: 40px;
  text-align: center;
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
  max-width: 200px;
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

@media (max-width: 768px) {
  .mold-use-records-container {
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

