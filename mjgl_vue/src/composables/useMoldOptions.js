import { ref, onMounted } from 'vue'
import { fetchMolds, queryMolds } from '@/api/molds'

/**
 * 供查询区「选择模具」下拉框复用：拉取模具列表，返回 moldOptions 与 load 方法。
 * @param {{ immediate?: boolean }} options - immediate 为 true 时在 onMounted 时自动加载（默认 true）
 */
export function useMoldOptions(options = {}) {
  const { immediate = true } = options
  const moldOptions = ref([])
  const moldOptionsLoading = ref(false)
  const moldOptionsLoaded = ref(false)

  const normalizeList = (res) => {
    const data = res?.data
    if (Array.isArray(data)) return data
    if (Array.isArray(data?.list)) return data.list
    if (Array.isArray(data?.records)) return data.records
    return []
  }

  const loadMoldOptions = async () => {
    if (moldOptionsLoaded.value || moldOptionsLoading.value) return
    moldOptionsLoading.value = true
    try {
      const res = await fetchMolds(1, 2000)
      let options = normalizeList(res)
      // 兼容后端接口切换：如果 allmolds 未返回列表，回退到 query 接口
      if (!options.length) {
        const fallbackRes = await queryMolds({}, 1, 2000)
        options = normalizeList(fallbackRes)
      }
      moldOptions.value = options
      moldOptionsLoaded.value = true
    } catch (e) {
      console.error('加载模具列表失败', e)
      moldOptions.value = []
    } finally {
      moldOptionsLoading.value = false
    }
  }

  onMounted(() => {
    if (immediate) loadMoldOptions()
  })

  return { moldOptions, moldOptionsLoading, moldOptionsLoaded, loadMoldOptions }
}
