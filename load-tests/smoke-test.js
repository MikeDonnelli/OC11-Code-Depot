/**
 * k6 Smoke Test - Quick Health Check
 * 
 * Test rapide avec 1-5 utilisateurs pour vérifier que tout fonctionne
 * À exécuter avant les tests de charge complets
 */

import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 3,
  duration: '30s',
  
  thresholds: {
    'http_req_duration': ['p(95)<1000'],
    'http_req_failed': ['rate<0.01'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'https://localhost';
const params = {
  insecureSkipTLSVerify: true,
};

export default function () {
  // Test 1: Liste des hôpitaux
  const listResponse = http.get(`${BASE_URL}/api/hospitals`, params);
  check(listResponse, {
    'smoke: list status 200': (r) => r.status === 200,
  });
  
  sleep(1);
  
  // Test 2: Recherche avec distance
  const searchResponse = http.get(
    `${BASE_URL}/api/hospitals?latitude=48.8566&longitude=2.3522`,
    params
  );
  check(searchResponse, {
    'smoke: search status 200': (r) => r.status === 200,
  });
  
  sleep(1);
}

export function handleSummary(data) {
  const passed = data.metrics.checks.values.passes === data.metrics.checks.values.value;
  console.log(passed ? '✅ Smoke test PASSED' : '❌ Smoke test FAILED');
  
  return {
    stdout: `Smoke Test: ${passed ? 'PASS ✅' : 'FAIL ❌'}\n`,
  };
}
