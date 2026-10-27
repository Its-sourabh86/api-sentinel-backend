import http from 'k6/http';
import { check } from 'k6';

export const options = {
  scenarios: {
    read: {
      executor: 'constant-vus',
      vus: 50,
      duration: '1m',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
  },
};

export default function () {
  const id = 1 + Math.floor(Math.random() * 200);
  const res = http.get(`http://localhost:8081/api/v1/monitors/${id}/results?page=0&size=20`);
  check(res, { 'status is 200': (r) => r.status === 200 });
}