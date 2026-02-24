<template>
  <div class="mold-management-container">
    <!-- 左侧边栏 -->
    <aside class="sidebar">
      <div class="sidebar-header">
        <div class="sidebar-logo">管理系统</div>
      </div>
      <nav class="sidebar-menu">
        <div
          class="menu-item"
          :class="{ active: $route.path === '/user-management' }"
          @click="goUserManagement"
        >
          <span class="menu-icon">👤</span>
          <span>用户管理</span>
        </div>
        <div
          class="menu-item"
          :class="{ active: $route.path === '/mold-management' }"
        >
          <span class="menu-icon">🧱</span>
          <span>模具管理</span>
        </div>
        <div
          class="menu-item parent-item"
          @click="showOpsChildren = !showOpsChildren"
        >
          <span class="menu-icon">🛠</span>
          <span>运维管理</span>
          <span class="submenu-arrow">{{ showOpsChildren ? '▾' : '▸' }}</span>
        </div>
        <div
          v-if="showOpsChildren"
          class="menu-item child-item"
          :class="{ active: $route.path === '/mold-use-records' }"
          @click="goOps"
        >
          <span class="menu-icon">📒</span>
          <span>使用记录</span>
        </div>
        <div class="menu-item disabled">
          <span class="menu-icon">📈</span>
          <span>监测与异常</span>
        </div>
        <div class="menu-item disabled">
          <span class="menu-icon">❤️</span>
          <span>健康评估</span>
        </div>
      </nav>
      <div class="sidebar-footer">
        <span class="sidebar-username">{{ authStore.username }}</span>
        <button class="sidebar-logout" @click="handleLogout">退出登录</button>
      </div>
    </aside>

    <!-- 右侧主区域 -->
    <div class="layout-main">
      <!-- 顶部条 -->
      <header class="top-header">
        <div class="top-title">模具管理</div>
        <div class="top-subtitle">管理模具台账与技术参数</div>
      </header>

      <!-- 主内容区域 -->
      <main class="main-content">
        <div class="content-wrapper">
          <section>
            <div class="card mold-list-card">
              <div class="card-header">
                <h3 class="card-title">模具列表</h3>
                <button @click="handleShowAddDialog" class="primary-btn">
                  <span class="btn-icon">+</span>
                  新建模具
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

                <!-- 查询条件 -->
                <div class="query-form">
                  <div class="query-row">
                    <div class="query-item">
                      <label>关键词</label>
                      <input
                        v-model="queryParams.keyword"
                        type="text"
                        class="form-input query-input"
                        placeholder="模具编号/名称"
                        @keyup.enter="handleQuery"
                      />
                    </div>
                    <div class="query-item">
                      <label>类别</label>
                      <input
                        v-model="queryParams.category"
                        type="text"
                        class="form-input query-input"
                        placeholder="如：注塑模、冲压模"
                        @keyup.enter="handleQuery"
                      />
                    </div>
                    <div class="query-item">
                      <label>当前状态</label>
                      <select v-model="queryParams.currentStatus" class="form-input query-input">
                        <option :value="null">全部</option>
                        <option :value="1">在库</option>
                        <option :value="2">使用中</option>
                        <option :value="3">维修中</option>
                        <option :value="4">外借</option>
                        <option :value="5">待报废</option>
                      </select>
                    </div>
                    <div class="query-item">
                      <label>制造商</label>
                      <input
                        v-model="queryParams.manuFacturer"
                        type="text"
                        class="form-input query-input"
                        placeholder="模糊搜索"
                        @keyup.enter="handleQuery"
                      />
                    </div>
                    <div class="query-item">
                      <label>所属项目</label>
                      <input
                        v-model="queryParams.productProject"
                        type="text"
                        class="form-input query-input"
                        placeholder="模糊搜索"
                        @keyup.enter="handleQuery"
                      />
                    </div>
                    <div class="query-item">
                      <label>创建人</label>
                      <input
                        v-model="queryParams.createdBy"
                        type="text"
                        class="form-input query-input"
                        placeholder="模糊搜索"
                        @keyup.enter="handleQuery"
                      />
                    </div>
                  </div>
                  <div class="query-actions">
                    <button type="button" class="primary-btn" @click="handleQuery">查询</button>
                    <button type="button" class="secondary-btn" @click="handleResetQuery">重置</button>
                  </div>
                </div>

                <div v-if="listLoading" class="table-loading">模具列表加载中...</div>
                <div v-else>
                  <div class="table-wrapper">
                  <table class="mold-table">
                    <thead>
                      <tr>
                        <th>模具编号</th>
                        <th>名称/型号</th>
                        <th>类别</th>
                        <th>所属项目</th>
                        <th>存放位置</th>
                        <th v-if="authStore.isAdmin">制造商</th>
                        <th v-if="authStore.isAdmin">采购日期</th>
                        <th>当前状态</th>
                        <th>累计使用次数</th>
                        <th>累计生产时长(h)</th>
                        <th>最后保养时间</th>
                        <th v-if="authStore.isAdmin">采购成本(元)</th>
                        <th>操作</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-if="!moldPage.list || moldPage.list.length === 0">
                        <td colspan="14" class="empty-cell">暂无模具数据</td>
                      </tr>
                      <tr v-for="item in moldPage.list" :key="item.id">
                        <td>{{ item.moldCode }}</td>
                        <td>{{ item.name }}</td>
                        <td>{{ item.category || '-' }}</td>
                        <td>{{ item.productProject || '-' }}</td>
                        <td>{{ item.location || '-' }}</td>
                        <td v-if="authStore.isAdmin">{{ item.manufacturer || '-' }}</td>
                        <td v-if="authStore.isAdmin">{{ formatDate(item.purchaseDate) }}</td>
                        <td>
                          <span :class="getStatusClass(item.currentStatus)">
                            {{ formatStatus(item.currentStatus) }}
                          </span>
                        </td>
                        <td>{{ item.totalUsageCount ?? '-' }}</td>
                        <td>{{ item.totalProductionTime ?? '-' }}</td>
                        <td>{{ formatDate(item.lastMaintenanceDate) }}</td>
                        <td v-if="authStore.isAdmin">{{ item.purchaseCost ?? '-' }}</td>
                        <td>
                          <div class="action-buttons">
                            <button
                              class="action-btn"
                              @click="handleViewDetail(item)"
                            >
                              详情
                            </button>
                            <button
                              class="action-btn"
                              @click="handleViewUseRecords(item)"
                            >
                              使用记录
                            </button>
                            <button
                              class="action-btn edit-btn"
                              v-if="authStore.isAdmin"
                              @click="handleEditMold(item)"
                            >
                              编辑
                            </button>
                            <button
                              class="action-btn delete-btn"
                              v-if="authStore.isAdmin"
                              @click="handleDeleteMold(item)"
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
                  <div v-if="moldPage.pages && moldPage.pages > 1" class="pagination">
                    <button
                      class="page-btn"
                      :disabled="pageNum === 1"
                      @click="changePage(pageNum - 1)"
                    >
                      上一页
                    </button>
                    <span class="page-info">
                      第 {{ pageNum }} / {{ moldPage.pages }} 页，
                      共 {{ moldPage.total || 0 }} 条
                    </span>
                    <button
                      class="page-btn"
                      :disabled="pageNum === moldPage.pages"
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

    <!-- 新建/编辑模具对话框 -->
    <div v-if="showEditDialog" class="dialog-overlay" @click="closeEditDialog">
      <div class="dialog-content" @click.stop>
        <div class="dialog-header">
          <h3>{{ editingMold && editingMold.id ? '编辑模具' : '新建模具' }}</h3>
          <button class="dialog-close" @click="closeEditDialog">×</button>
        </div>
        <div class="dialog-body">
          <form @submit.prevent="handleSaveMold" class="mold-form">
            <div class="form-section-title">基本信息</div>
            <div class="form-row">
              <div class="form-group">
                <label for="moldCode">模具编号 *</label>
                <input
                  id="moldCode"
                  v-model="form.moldCode"
                  type="text"
                  class="form-input"
                  required
                  placeholder="请输入模具编号（唯一）"
                />
              </div>
              <div class="form-group">
                <label for="name">名称/型号 *</label>
                <input
                  id="name"
                  v-model="form.name"
                  type="text"
                  class="form-input"
                  required
                  placeholder="请输入模具名称或型号"
                />
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label for="category">类别</label>
                <input
                  id="category"
                  v-model="form.category"
                  type="text"
                  class="form-input"
                  placeholder="如：注塑模、冲压模"
                />
              </div>
              <div class="form-group">
                <label for="productProject">所属产品/项目</label>
                <input
                  id="productProject"
                  v-model="form.productProject"
                  type="text"
                  class="form-input"
                  placeholder="请输入所属产品或项目"
                />
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label for="location">存放位置</label>
                <input
                  id="location"
                  v-model="form.location"
                  type="text"
                  class="form-input"
                  placeholder="仓库-货架-库位"
                />
              </div>
              <div class="form-group">
                <label for="manufacturer">制造商</label>
                <input
                  id="manufacturer"
                  v-model="form.manufacturer"
                  type="text"
                  class="form-input"
                  placeholder="请输入制造商"
                />
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label for="supplier">供应商</label>
                <input
                  id="supplier"
                  v-model="form.supplier"
                  type="text"
                  class="form-input"
                  placeholder="请输入供应商"
                />
              </div>
              <div class="form-group">
                <label for="purchaseDate">采购/入库日期</label>
                <input
                  id="purchaseDate"
                  v-model="form.purchaseDate"
                  type="datetime-local"
                  step="3600"
                  class="form-input"
                />
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label for="currentStatus">当前状态</label>
                <select
                  id="currentStatus"
                  v-model.number="form.currentStatus"
                  class="form-input"
                >
                  <option :value="null">请选择</option>
                  <option :value="1">在库</option>
                  <option :value="2">使用中</option>
                  <option :value="3">维修中</option>
                  <option :value="4">外借</option>
                  <option :value="5">待报废</option>
                </select>
              </div>
              <div class="form-group">
                <label for="purchaseCost">采购成本(元)</label>
                <input
                  id="purchaseCost"
                  v-model.number="form.purchaseCost"
                  type="number"
                  min="0"
                  step="0.01"
                  class="form-input"
                  placeholder="请输入采购成本"
                />
              </div>
            </div>

            <div class="form-section-title">技术参数</div>
            <div class="form-row">
              <div class="form-group">
                <label for="dimensions">外形尺寸</label>
                <input
                  id="dimensions"
                  v-model="form.specs.dimensions"
                  type="text"
                  class="form-input"
                  placeholder="长×宽×高×重"
                />
              </div>
              <div class="form-group">
                <label for="material">材质</label>
                <input
                  id="material"
                  v-model="form.specs.material"
                  type="text"
                  class="form-input"
                  placeholder="请输入模具材质"
                />
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label for="cavityCount">腔数</label>
                <input
                  id="cavityCount"
                  v-model.number="form.specs.cavityCount"
                  type="number"
                  min="0"
                  class="form-input"
                />
              </div>
              <div class="form-group">
                <label for="designLifeCycles">设计寿命(次)</label>
                <input
                  id="designLifeCycles"
                  v-model.number="form.specs.designLifeCycles"
                  type="number"
                  min="0"
                  class="form-input"
                />
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label for="designLifeYears">设计寿命(年)</label>
                <input
                  id="designLifeYears"
                  v-model.number="form.specs.designLifeYears"
                  type="number"
                  min="0"
                  class="form-input"
                />
              </div>
              <div class="form-group">
                <label for="keyDimensions">关键尺寸与公差</label>
                <input
                  id="keyDimensions"
                  v-model="form.specs.keyDimensions"
                  type="text"
                  class="form-input"
                  placeholder="请输入关键尺寸与公差"
                />
              </div>
            </div>

            <div class="form-group">
              <label for="linkedDocuments">关联文档路径</label>
              <input
                id="linkedDocuments"
                v-model="form.specs.linkedDocuments"
                type="text"
                class="form-input"
                placeholder="JSON或逗号分隔的文档路径"
              />
            </div>

            <div class="form-section-title">二维码设置</div>
            <div class="form-group">
              <label for="qrcodeType">二维码类型</label>
              <select
                id="qrcodeType"
                v-model.number="form.qrcodeType"
                class="form-input"
              >
                <option :value="null">请选择</option>
                <option :value="1">模具详情</option>
                <option :value="2">快速报修</option>
                <option :value="3">异常上报</option>
              </select>
            </div>

            <div class="form-section-title">附件上传</div>
            <div class="form-row">
              <div class="form-group">
                <label for="fileType">文件类型</label>
                <select
                  id="fileType"
                  v-model="uploadFileType"
                  class="form-input"
                >
                  <option value="photo">照片</option>
                  <option value="drawing">图纸</option>
                  <option value="bom">BOM</option>
                  <option value="manual">说明书</option>
                  <option value="repair_photo">维修照片</option>
                </select>
              </div>
              <div class="form-group">
                <label for="fileDescription">文件说明</label>
                <input
                  id="fileDescription"
                  v-model="uploadDescription"
                  type="text"
                  class="form-input"
                  placeholder="例如：模具正面照、装配图等"
                />
              </div>
            </div>
            <div class="form-group">
              <label for="files">选择文件（可多选）</label>
              <input
                id="files"
                type="file"
                multiple
                class="form-input"
                @change="handleFilesChange"
              />
              <div
                v-if="selectedFiles.length"
                class="selected-files"
              >
                已选择 {{ selectedFiles.length }} 个文件：
                <span
                  v-for="file in selectedFiles"
                  :key="file.name + file.size"
                  class="selected-file-item"
                >
                  {{ file.name }}
                </span>
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
              <button type="button" class="cancel-button" @click="closeEditDialog">
                取消
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- 设备详情对话框（含技术参数与附件上传） -->
    <div
      v-if="showDetailDialog && detailMold"
      class="dialog-overlay"
      @click="closeDetailDialog"
    >
      <div class="dialog-content" @click.stop>
        <div class="dialog-header">
          <h3>设备详情 - {{ detailMold.moldCode }}</h3>
          <button class="dialog-close" @click="closeDetailDialog">×</button>
        </div>
        <div class="dialog-body">
          <div class="detail-section">
            <div class="detail-title">基本信息</div>
            <div class="detail-grid">
              <div class="detail-item">
                <span class="label">名称/型号</span>
                <span class="value">{{ detailMold.name || '-' }}</span>
              </div>
              <div class="detail-item">
                <span class="label">类别</span>
                <span class="value">{{ detailMold.category || '-' }}</span>
              </div>
              <div class="detail-item">
                <span class="label">所属项目</span>
                <span class="value">{{ detailMold.productProject || '-' }}</span>
              </div>
              <div class="detail-item">
                <span class="label">存放位置</span>
                <span class="value">{{ detailMold.location || '-' }}</span>
              </div>
              <div class="detail-item">
                <span class="label">当前状态</span>
                <span class="value">{{ formatStatus(detailMold.currentStatus) }}</span>
              </div>
              <div class="detail-item">
                <span class="label">累计使用次数</span>
                <span class="value">{{ detailMold.totalUsageCount ?? '-' }}</span>
              </div>
              <div class="detail-item">
                <span class="label">累计生产时长(h)</span>
                <span class="value">{{ detailMold.totalProductionTime ?? '-' }}</span>
              </div>
              <div class="detail-item">
                <span class="label">最后保养时间</span>
                <span class="value">{{ formatDate(detailMold.lastMaintenanceDate) }}</span>
              </div>
              <div
                v-if="authStore.isAdmin"
                class="detail-item"
              >
                <span class="label">制造商</span>
                <span class="value">{{ detailMold.manufacturer || '-' }}</span>
              </div>
              <div
                v-if="authStore.isAdmin"
                class="detail-item"
              >
                <span class="label">采购日期</span>
                <span class="value">{{ formatDate(detailMold.purchaseDate) }}</span>
              </div>
              <div
                v-if="authStore.isAdmin"
                class="detail-item"
              >
                <span class="label">采购成本(元)</span>
                <span class="value">{{ detailMold.purchaseCost ?? '-' }}</span>
              </div>
            </div>
          </div>

          <div class="detail-section">
            <div class="detail-title">技术参数</div>
            <div class="detail-grid">
              <div class="detail-item">
                <span class="label">外形尺寸</span>
                <span class="value">{{ detailMold.specs?.dimensions || '-' }}</span>
              </div>
              <div class="detail-item">
                <span class="label">材质</span>
                <span class="value">{{ detailMold.specs?.material || '-' }}</span>
              </div>
              <div class="detail-item">
                <span class="label">腔数</span>
                <span class="value">{{ detailMold.specs?.cavityCount ?? '-' }}</span>
              </div>
              <div class="detail-item">
                <span class="label">设计寿命(次)</span>
                <span class="value">{{ detailMold.specs?.designLifeCycles ?? '-' }}</span>
              </div>
              <div class="detail-item">
                <span class="label">设计寿命(年)</span>
                <span class="value">{{ detailMold.specs?.designLifeYears ?? '-' }}</span>
              </div>
              <div class="detail-item detail-item-full">
                <span class="label">关键尺寸与公差</span>
                <span class="value">{{ detailMold.specs?.keyDimensions || '-' }}</span>
              </div>
            </div>
          </div>

          <div class="detail-section">
            <div class="detail-title">附件列表</div>
            <div v-if="!detailMold.files || detailMold.files.length === 0" class="no-files">
              暂无附件
            </div>
            <ul
              v-else
              class="file-list"
            >
              <li
                v-for="file in detailMold.files"
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
                  v-if="authStore.isAdmin"
                  type="button"
                  class="file-delete-btn"
                  @click="handleDeleteFile(file)"
                >
                  删除
                </button>
              </li>
            </ul>
          </div>

          <div class="detail-section">
            <div class="detail-title">上传附件</div>
            <div class="form-row">
              <div class="form-group">
                <label for="detailFileType">文件类型</label>
                <select
                  id="detailFileType"
                  v-model="detailUploadFileType"
                  class="form-input"
                >
                  <option value="photo">照片</option>
                  <option value="drawing">图纸</option>
                  <option value="bom">BOM</option>
                  <option value="manual">说明书</option>
                  <option value="repair_photo">维修照片</option>
                </select>
              </div>
              <div class="form-group">
                <label for="detailFileDescription">文件说明</label>
                <input
                  id="detailFileDescription"
                  v-model="detailUploadDescription"
                  type="text"
                  class="form-input"
                  placeholder="例如：模具正面照、装配图等"
                />
              </div>
            </div>
            <div class="form-group">
              <label for="detailFiles">选择文件（可多选）</label>
              <input
                id="detailFiles"
                type="file"
                multiple
                class="form-input"
                @change="handleDetailFilesChange"
              />
              <div
                v-if="detailSelectedFiles.length"
                class="selected-files"
              >
                已选择 {{ detailSelectedFiles.length }} 个文件：
                <span
                  v-for="file in detailSelectedFiles"
                  :key="file.name + file.size"
                  class="selected-file-item"
                >
                  {{ file.name }}
                </span>
              </div>
            </div>
            <div class="dialog-actions">
              <button
                type="button"
                class="submit-button"
                :disabled="detailUploadLoading || !detailSelectedFiles.length || !detailMold.id"
                @click="handleUploadForDetail"
              >
                {{ detailUploadLoading ? '上传中...' : '上传附件' }}
              </button>
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

    <!-- 模具删除确认对话框 -->
    <div
      v-if="showConfirmDialog"
      class="dialog-overlay"
      @click="showConfirmDialog = false"
    >
      <div class="confirm-dialog" @click.stop>
        <div class="confirm-icon danger">
          🗑️
        </div>
        <h3 class="confirm-title">删除模具</h3>
        <p class="confirm-message">
          确定要删除模具 "{{ deletingMold?.moldCode }}" 吗？此操作不可恢复！
        </p>
        <div class="confirm-actions">
          <button
            class="confirm-cancel-btn"
            @click="showConfirmDialog = false"
          >
            取消
          </button>
          <button
            class="confirm-ok-btn danger"
            @click="confirmDelete"
          >
            删除
          </button>
        </div>
      </div>
    </div>

    <!-- 附件删除确认对话框 -->
    <div
      v-if="showFileConfirmDialog"
      class="dialog-overlay"
      @click="showFileConfirmDialog = false"
    >
      <div class="confirm-dialog" @click.stop>
        <div class="confirm-icon danger">
          🗑️
        </div>
        <h3 class="confirm-title">删除附件</h3>
        <p class="confirm-message">
          确定要删除附件 "{{ deletingFile?.originalName || deletingFile?.filePath }}" 吗？此操作不可恢复！
        </p>
        <div class="confirm-actions">
          <button
            class="confirm-cancel-btn"
            @click="showFileConfirmDialog = false"
          >
            取消
          </button>
          <button
            class="confirm-ok-btn danger"
            @click="confirmDeleteFile"
          >
            删除
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { fetchMolds, queryMolds, createMold, updateMold, deleteMold } from '@/api/molds'
import { uploadBizFiles, getFilePreviewUrl, deleteFiles } from '@/api/files'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const showOpsChildren = ref(true)

const pageNum = ref(1)
const pageSize = ref(10)
const moldPage = reactive({
  list: [],
  total: 0,
  pages: 0,
})

// 查询条件（与后端 MoldQueryParam 对应，注意制造商字段为 manuFacturer）
const queryParams = reactive({
  keyword: '',
  category: '',
  currentStatus: null,
  manuFacturer: '',
  productProject: '',
  createdBy: '',
})

const listLoading = ref(false)
const saveLoading = ref(false)
const successMessage = ref('')
const errorMessage = ref('')
const dialogErrorMessage = ref('')

const showEditDialog = ref(false)
const editingMold = ref(null)
const showConfirmDialog = ref(false)
const deletingMold = ref(null)

// 附件删除确认
const showFileConfirmDialog = ref(false)
const deletingFile = ref(null)

// 上传相关
const selectedFiles = ref([])
const uploadFileType = ref('photo')
const uploadDescription = ref('')

// 详情对话框相关
const showDetailDialog = ref(false)
const detailMold = ref(null)
const detailSelectedFiles = ref([])
const detailUploadFileType = ref('photo')
const detailUploadDescription = ref('')
const detailUploadLoading = ref(false)

const emptyForm = () => ({
  id: null,
  moldCode: '',
  name: '',
  category: '',
  productProject: '',
  location: '',
  manufacturer: '',
  supplier: '',
  purchaseDate: '',
  currentStatus: null,
  purchaseCost: null,
  // 技术参数
  specs: {
    dimensions: '',
    material: '',
    cavityCount: null,
    designLifeCycles: null,
    designLifeYears: null,
    keyDimensions: '',
    linkedDocuments: '',
  },
  // 二维码类型
  qrcodeType: null,
})

const form = ref(emptyForm())

const hasQueryCondition = () => {
  return (
    (queryParams.keyword && queryParams.keyword.trim()) ||
    (queryParams.category && queryParams.category.trim()) ||
    queryParams.currentStatus != null ||
    (queryParams.manuFacturer && queryParams.manuFacturer.trim()) ||
    (queryParams.productProject && queryParams.productProject.trim()) ||
    (queryParams.createdBy && queryParams.createdBy.trim())
  )
}

const loadMolds = async () => {
  listLoading.value = true
  errorMessage.value = ''
  try {
    const res = hasQueryCondition()
      ? await queryMolds(
          {
            keyword: queryParams.keyword?.trim() || undefined,
            category: queryParams.category?.trim() || undefined,
            currentStatus: queryParams.currentStatus ?? undefined,
            manuFacturer: queryParams.manuFacturer?.trim() || undefined,
            productProject: queryParams.productProject?.trim() || undefined,
            createdBy: queryParams.createdBy?.trim() || undefined,
          },
          pageNum.value,
          pageSize.value
        )
      : await fetchMolds(pageNum.value, pageSize.value)
    moldPage.list = res.data?.list ?? []
    moldPage.total = res.data?.total ?? 0
    moldPage.pages = res.data?.pages ?? 0
  } catch (e) {
    errorMessage.value = e.message || '加载模具列表失败'
  } finally {
    listLoading.value = false
  }
}

const handleQuery = () => {
  pageNum.value = 1
  loadMolds()
}

const handleResetQuery = () => {
  queryParams.keyword = ''
  queryParams.category = ''
  queryParams.currentStatus = null
  queryParams.manuFacturer = ''
  queryParams.productProject = ''
  queryParams.createdBy = ''
  pageNum.value = 1
  loadMolds()
}

const changePage = (newPage) => {
  if (newPage < 1 || (moldPage.pages && newPage > moldPage.pages)) {
    return
  }
  pageNum.value = newPage
  loadMolds()
}

const handleShowAddDialog = () => {
  editingMold.value = null
  form.value = emptyForm()
  dialogErrorMessage.value = ''
  selectedFiles.value = []
  uploadFileType.value = 'photo'
  uploadDescription.value = ''
  showEditDialog.value = true
}

const handleEditMold = (item) => {
  editingMold.value = item
  form.value = {
    id: item.id,
    moldCode: item.moldCode,
    name: item.name,
    category: item.category,
    productProject: item.productProject,
    location: item.location,
    manufacturer: item.manufacturer,
    supplier: item.supplier,
    purchaseDate: item.purchaseDate
      ? item.purchaseDate.replace(' ', 'T')
      : '',
    currentStatus: item.currentStatus ?? null,
    purchaseCost: item.purchaseCost ?? null,
    specs: {
      dimensions: item.specs?.dimensions || '',
      material: item.specs?.material || '',
      cavityCount: item.specs?.cavityCount ?? null,
      designLifeCycles: item.specs?.designLifeCycles ?? null,
      designLifeYears: item.specs?.designLifeYears ?? null,
      keyDimensions: item.specs?.keyDimensions || '',
      linkedDocuments: item.specs?.linkedDocuments || '',
    },
    qrcodeType: item.qrcode?.qrcodeType ?? null,
  }
  dialogErrorMessage.value = ''
  selectedFiles.value = []
  uploadFileType.value = 'photo'
  uploadDescription.value = ''
  showEditDialog.value = true
}

const closeEditDialog = () => {
  showEditDialog.value = false
}

const handleSaveMold = async () => {
  dialogErrorMessage.value = ''

  if (!form.value.moldCode || !form.value.name) {
    dialogErrorMessage.value = '请填写必填项：模具编号、名称/型号'
    return
  }

  let purchaseDateStr = form.value.purchaseDate
    ? form.value.purchaseDate.replace('T', ' ')
    : null
  if (purchaseDateStr) {
    // 统一只保留“到小时”，分钟与秒一律 00
    const [datePart, timePart] = purchaseDateStr.split(' ')
    const hour = timePart ? timePart.split(':')[0] : '00'
    purchaseDateStr = `${datePart} ${hour}:00:00`
  }
  const payload = {
    ...form.value,
    purchaseDate: purchaseDateStr,
  }

  saveLoading.value = true
  try {
    let moldId = form.value.id
    if (form.value.id) {
      await updateMold(payload)
      successMessage.value = '更新模具成功'
    } else {
      const res = await createMold(payload)
      moldId = res.data?.id
      successMessage.value = '创建模具成功'
    }

    // 如果选择了文件，并且拿到了模具ID，则上传附件
    if (moldId && selectedFiles.value.length) {
      await uploadBizFiles(moldId, selectedFiles.value, {
        bizType: 'mold',
        fileType: uploadFileType.value,
        description: uploadDescription.value,
      })
    }
    showEditDialog.value = false
    await loadMolds()
    setTimeout(() => (successMessage.value = ''), 3000)
  } catch (e) {
    dialogErrorMessage.value = e.message || '保存模具失败'
  } finally {
    saveLoading.value = false
  }
}

const handleDeleteMold = (item) => {
  deletingMold.value = item
  showConfirmDialog.value = true
}

const handleViewDetail = (item) => {
  detailMold.value = item
  detailSelectedFiles.value = []
  detailUploadFileType.value = 'photo'
  detailUploadDescription.value = ''
  showDetailDialog.value = true
}

const closeDetailDialog = () => {
  showDetailDialog.value = false
}

const confirmDelete = async () => {
  if (!deletingMold.value || !deletingMold.value.id) {
    showConfirmDialog.value = false
    return
  }
  try {
    await deleteMold(deletingMold.value.id)
    successMessage.value = '删除模具成功'
    showConfirmDialog.value = false
    await loadMolds()
    setTimeout(() => (successMessage.value = ''), 3000)
  } catch (e) {
    errorMessage.value = e.message || '删除模具失败'
  }
}

const formatStatus = (status) => {
  if (!status) return '-'
  const map = {
    1: '在库',
    2: '使用中',
    3: '维修中',
    4: '外借',
    5: '待报废',
  }
  return map[status] || status
}

const getStatusClass = (status) => {
  if (!status) return ''
  if (status === 2 || status === 3 || status === 4) {
    return 'status-warning'
  }
  if (status === 5) {
    return 'status-danger'
  }
  return 'status-normal'
}

const formatQrcodeType = (type) => {
  if (!type) return '-'
  const map = {
    1: '详情',
    2: '报修',
    3: '异常上报',
  }
  return map[type] || type
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  if (typeof dateStr === 'string' && dateStr.includes(' ')) {
    return dateStr
  }
  const date = new Date(dateStr)
  if (Number.isNaN(date.getTime())) return dateStr
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

const handleFilesChange = (event) => {
  const files = Array.from(event.target.files || [])
  selectedFiles.value = files
}

const handleDetailFilesChange = (event) => {
  const files = Array.from(event.target.files || [])
  detailSelectedFiles.value = files
}

const handleUploadForDetail = async () => {
  if (!detailMold.value || !detailMold.value.id || !detailSelectedFiles.value.length) {
    return
  }
  detailUploadLoading.value = true
  try {
    await uploadBizFiles(detailMold.value.id, detailSelectedFiles.value, {
      bizType: 'mold',
      fileType: detailUploadFileType.value,
      description: detailUploadDescription.value,
    })
    // 刷新列表数据，以便详情中的附件列表更新
    await loadMolds()
    const refreshed = moldPage.list.find((m) => m.id === detailMold.value.id)
    if (refreshed) {
      detailMold.value = refreshed
    }
    detailSelectedFiles.value = []
    detailUploadDescription.value = ''
  } catch (e) {
    errorMessage.value = e.message || '上传附件失败'
  } finally {
    detailUploadLoading.value = false
  }
}

const handlePreviewFile = async (file) => {
  if (!file || !file.id) {
    return
  }
  try {
    const res = await getFilePreviewUrl(file.id)
    const url = res.data
    if (url) {
      window.open(url, '_blank')
    }
  } catch (e) {
    errorMessage.value = e.message || '获取预览链接失败'
  }
}

const handleDeleteFile = (file) => {
  if (!file || !file.id) {
    return
  }
  deletingFile.value = file
  showFileConfirmDialog.value = true
}

const confirmDeleteFile = async () => {
  if (!deletingFile.value || !deletingFile.value.id) {
    showFileConfirmDialog.value = false
    return
  }
  try {
    await deleteFiles([deletingFile.value.id])
    // 删除成功后刷新当前模具的附件列表
    await loadMolds()
    const refreshed = moldPage.list.find((m) => m.id === detailMold.value.id)
    if (refreshed) {
      detailMold.value = refreshed
    }
    showFileConfirmDialog.value = false
  } catch (e) {
    errorMessage.value = e.message || '删除附件失败'
  }
}

const goOps = () => {
  router.push('/mold-use-records')
}

const handleViewUseRecords = (item) => {
  if (!item || !item.id) {
    return
  }
  router.push({
    path: '/mold-use-records',
    query: {
      moldId: item.id,
      moldCode: item.moldCode,
      moldName: item.name,
      moldCategory: item.category,
    },
  })
}

onMounted(() => {
  if (!authStore.isAuthenticated) {
    router.push('/login')
    return
  }
  // 从查询参数恢复分页（可选）
  const queryPageNum = Number(route.query.pageNum || 1)
  const queryPageSize = Number(route.query.pageSize || 10)
  pageNum.value = Number.isNaN(queryPageNum) ? 1 : queryPageNum
  pageSize.value = Number.isNaN(queryPageSize) ? 10 : queryPageSize
  loadMolds()
})
</script>

<style scoped>
.mold-management-container {
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
  justify-content: space-between;
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
  min-width: 120px;
  flex: 1;
  max-width: 180px;
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

.btn-icon {
  font-size: 18px;
  font-weight: 600;
  line-height: 1;
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

.mold-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.table-wrapper {
  width: 100%;
  overflow-x: auto;
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

.delete-btn {
  background: #ef4444;
  color: #ffffff;
}

.delete-btn:hover {
  background: #dc2626;
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

.pagination {
  margin-top: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-btn {
  padding: 6px 12px;
  border-radius: 4px;
  border: 1px solid #d1d5db;
  background: #ffffff;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s;
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
  .mold-management-container {
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

/* 详情页样式优化 */
.detail-section {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e5e7eb;
}

.detail-section:last-of-type {
  border-bottom: none;
  margin-bottom: 0;
}

.detail-title {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 12px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px 24px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.detail-item-full {
  grid-column: 1 / -1;
}

.detail-item .label {
  font-size: 12px;
  color: #6b7280;
}

.detail-item .value {
  font-size: 14px;
  color: #111827;
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

.file-name {
  font-size: 14px;
  color: #111827;
}

.file-meta {
  font-size: 12px;
  color: #6b7280;
}

.file-main {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.file-preview-btn {
  padding: 4px 10px;
  border-radius: 4px;
  border: 1px solid #d1d5db;
  background: #ffffff;
  font-size: 12px;
  cursor: pointer;
  white-space: nowrap;
}

.file-preview-btn:hover {
  background: #f3f4f6;
}

.file-delete-btn {
  padding: 4px 10px;
  border-radius: 4px;
  border: 1px solid #fecaca;
  background: #fef2f2;
  font-size: 12px;
  cursor: pointer;
  white-space: nowrap;
  color: #b91c1c;
}

.file-delete-btn:hover {
  background: #fee2e2;
}

@media (max-width: 768px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
