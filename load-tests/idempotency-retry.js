import http from 'k6/http';
import { check } from 'k6';
import { Rate } from 'k6/metrics';

const apiUrl = __ENV.API_URL || 'http://localhost:8080/api/v1';
const productId = Number(__ENV.PRODUCT_ID);
const tableCode = __ENV.TABLE_CODE || 'MESA-01';
const unexpectedResponses = new Rate('unexpected_responses');

export const options = {
  vus: Number(__ENV.VUS || 10),
  iterations: Number(__ENV.ITERATIONS || 20),
  thresholds: {
    http_req_duration: ['p(95)<750'],
    unexpected_responses: ['rate==0'],
  },
};

export default function () {
  const key = `retry-${__VU}-${__ITER}-${Date.now()}`;
  const body = JSON.stringify({
    tableCode,
    items: [{ productId, quantity: 1, notes: 'prueba idempotente' }],
  });
  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Idempotency-Key': key,
    },
  };

  const first = http.post(`${apiUrl}/orders`, body, params);
  const retry = http.post(`${apiUrl}/orders`, body, params);
  const firstBody = first.status === 201 ? first.json() : null;
  const retryBody = retry.status === 200 ? retry.json() : null;
  const expected = first.status === 201
    && retry.status === 200
    && firstBody?.id === retryBody?.id
    && retry.headers['Idempotency-Replayed'] === 'true';

  unexpectedResponses.add(!expected);
  check(retry, {
    'el reintento devuelve el mismo pedido': () => expected,
  });
}

export function handleSummary(data) {
  return {
    stdout: JSON.stringify(data, null, 2),
    'load-tests/results/idempotency-retry.json': JSON.stringify(data),
  };
}
