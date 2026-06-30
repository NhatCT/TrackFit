export const getErrorMessage = (error, fallback = "Thao tac that bai") => {
  return error?.response?.data?.message || fallback;
};

export const normalizeListResponse = (data) => {
  const items = Array.isArray(data) ? data : (data?.items || []);
  const meta = {
    page: Array.isArray(data) ? 1 : (data?.page ?? 1),
    pageSize: Array.isArray(data) ? items.length : (data?.pageSize ?? items.length),
    totalPages: Array.isArray(data) ? 1 : (data?.totalPages ?? 1),
    totalElements: Array.isArray(data) ? items.length : (data?.totalElements ?? items.length),
  };
  return { items, meta };
};

export const confirmAndDelete = async (apiCall, confirmMsg, opts = {}) => {
  if (!window.confirm(confirmMsg)) return false;
  const { onSuccess, onError } = opts;
  try {
    await apiCall();
    if (onSuccess) onSuccess();
    return true;
  } catch (e) {
    if (onError) onError(e);
    return false;
  }
};
