import apiClient from './apiClient'

export const fileApi = {
  uploadImage: (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return apiClient.post('/files/images', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
}
