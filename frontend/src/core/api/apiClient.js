import axios from 'axios'

// Configuración base del cliente HTTP hacia el backend SGA
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8085/api/v1',
  headers: {
    'Content-Type': 'application/json'
  },
  timeout: 15000
})

// Interceptor de petición: Inyecta el token Bearer si existe
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('sga_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

let isRedirecting = false

// Interceptor de respuesta: Manejo centralizado de 401 y errores de red
apiClient.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    if (error.response && error.response.status === 401) {
      // Token expirado o inválido: limpiar sesión y redirigir al login
      localStorage.removeItem('sga_token')
      localStorage.removeItem('sga_user')
      if (!isRedirecting && window.location.pathname !== '/login') {
        isRedirecting = true
        window.location.replace('/login')
      }
    }
    return Promise.reject(error)
  }
)

export default apiClient
