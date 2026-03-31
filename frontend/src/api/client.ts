import axios from 'axios';

const client = axios.create({
  baseURL: '/api/admin',
  headers: { 'Content-Type': 'application/json' },
});

client.interceptors.response.use(
  (res) => res,
  (err) => {
    const message = err.response?.data?.message ?? err.message ?? 'Unexpected error';
    return Promise.reject(new Error(message));
  },
);

export default client;

