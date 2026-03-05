import request from '@/utils/request'

// 业务文件上传（绑定到某个业务ID，例如模具）
export const uploadBizFiles = (bizId, files, options = {}) => {
  const {
    bizType = 'mold',
    fileType = 'photo',
    description = '',
    imageStatus = '',
  } = options

  const formData = new FormData()
  files.forEach((file) => {
    formData.append('files', file)
  })
  formData.append('bizType', bizType)
  formData.append('bizId', bizId)
  formData.append('fileType', fileType)
  if (description) {
    formData.append('description', description)
  }
   if (imageStatus) {
    formData.append('imageStatus', imageStatus)
  }

  return request.post('/api/files/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

// 获取文件预览URL
export const getFilePreviewUrl = (id) =>
  request.get(`/api/files/preview/${id}`)

// 删除文件（支持批量）
export const deleteFiles = (ids) =>
  request.delete('/api/files/delete', {
    data: ids,
  })

// 通用：根据业务类型 + 业务ID（可选文件类型）查询文件列表
export const fetchBizFiles = (bizType, bizId, fileType) =>
  request.get('/api/files/biz', {
    params: {
      bizType,
      bizId,
      fileType,
    },
  })

