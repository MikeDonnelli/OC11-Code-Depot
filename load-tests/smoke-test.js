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
  
  insecureSkipTLSVerify: true,
  
  thresholds: {
    'http_req_duration': ['p(95)<1000'],
    'http_req_failed': ['rate<0.01'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'https://hospital-ui';

export default function () {
  // Test 1: Liste des hôpitaux
  const listResponse = http.get(`${BASE_URL}/api/hospitals`);
  const listOk = listResponse.status === 200;
  
  if (!listOk) {
    console.log(`❌ List endpoint failed: ${listResponse.status} - ${listResponse.body}`);
  }
  
  check(listResponse, {
    'smoke: list status 200': (r) => r.status === 200,
  });
  
  sleep(1);
  
  // Test 2: Recherche avec distance
  const searchResponse = http.get(
    `${BASE_URL}/api/hospitals?latitude=48.8566&longitude=2.3522`
  );
  const searchOk = searchResponse.status === 200;
  
  if (!searchOk) {
    console.log(`❌ Search endpoint failed: ${searchResponse.status} - ${searchResponse.body}`);
  }
  
  check(searchResponse, {
    'smoke: search status 200': (r) => r.status === 200,
  });
  
  sleep(1);
}
