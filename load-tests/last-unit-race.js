import http from 'k6/http';
import { check } from 'k6';
import { Counter, Rate } from 'k6/metrics';

const apiUrl = __ENV.API_URL || 'http://localhost:8080/api/v1';
const productId = Number(__ENV.PRODUCT_ID);
const tableCode = __ENV.TABLE_CODE || 'MESA-01';
const unexpectedResponses = new Rate('unexpected_responses');
const createdOrders = new Counter('created_orders');

export const options = {
  vus: Number(__ENV.VUS || 50),
  iterations: Number(__ENV.ITERATIONS || 50),
  thresholds: {
    http_req_duration: ['p(95)<750'],
    unexpected_responses: ['rate==0'],
    created_orders: ['count==1'],
  },
};

export default function () {
  const idempotencyKey = `race-${__VU}-${__ITER}-${Date.now()}`;
  const response = http.post(`${apiUrl}/orders`, JSON.stringify({
    tableCode,
    items: [{ productId, quantity: 1, notes: null }],
  }), {
    headers: {
      'Content-Type': 'application/json',
      'Idempotency-Key': idempotencyKey,
    },
  });

  const expected = response.status === 201 || response.status === 409;
  if (response.status === 201) {
    createdOrders.add(1);
  }
  unexpectedResponses.add(!expected);
  check(response, {
    'solo confirma o rechaza por conflicto': () => expected,
  });
}

export function handleSummary(data) {
  return {
    stdout: JSON.stringify(data, null, 2),
    'load-tests/results/last-unit-race.json': JSON.stringify(data),
  };
}
