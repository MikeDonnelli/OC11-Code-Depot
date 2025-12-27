<template>
  <div class="page-wrapper">
    <h2>Trouver l'hôpital le plus proche</h2>
    <form @submit.prevent="findNearest">
      <div class="specialty-row">
        <label>Spécialité :</label>
        <select v-model="specialty" required>
          <option value="">— Sélectionnez une spécialité —</option>
          <option v-for="s in specialties" :key="s" :value="s">{{ s }}</option>
        </select>
      </div>
      
      <div class="coords-row">
        <label>Localisation (lat, lon) :</label>
        <div class="coords-inputs">
          <input v-model.number="from.lat" placeholder="lat" required />
          <input v-model.number="from.lon" placeholder="lon" required />
        </div>
      </div>
      <button type="submit" :disabled="!specialty">Rechercher</button>
    </form>

    <div v-if="nearest">
      <h3>Hôpital le plus proche</h3>
      <div class="result-card">
        <h4>{{ nearest.hospital.name }}</h4>
        <p><strong>Spécialité :</strong> {{ nearest.searchedSpecialty }}</p>
        <p><strong>Lits disponibles :</strong> {{ getAvailableBeds(nearest.hospital, nearest.searchedSpecialty) }}</p>
        <p><strong>Distance :</strong> {{ nearest.distanceKm }} km</p>
        <p><strong>Temps de trajet estimé :</strong> {{ nearest.duration }}</p>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { findNearest as apiFindNearest } from '../services/api'

export default {
  setup() {
    const from = ref({ lat: 48.85, lon: 2.35 })
    const specialty = ref('')
    const specialties = ref([])
    const nearest = ref(null)

    async function loadSpecialties() {
      try {
        const response = await fetch('/api/hospitals/specialties')
        specialties.value = await response.json()
      } catch (error) {
        console.error('Failed to load specialties:', error)
      }
    }

    async function findNearest() {
      try {
        // For POC we will call the hospital endpoint with the full list fetched from /api/hospitals
        // In a real app we could present a list for the user to choose or fetch nearby hospitals.
        const hospitals = await (await fetch(`/api/hospitals?specialty=${specialty.value}&minBeds=1`)).json()
        const resp = await apiFindNearest({ from: from.value, hospitals })
        // Save the searched specialty with the result
        nearest.value = { ...resp, searchedSpecialty: specialty.value }
      } catch (error) {
        console.error('Failed to find nearest hospital:', error)
        nearest.value = null
      }
    }

    function getAvailableBeds(hospital, specialtyName) {
      const spec = hospital.specialties?.find(s => s.specialty.toLowerCase() === specialtyName.toLowerCase())
      return spec ? spec.availableBeds : 0
    }

    onMounted(() => {
      loadSpecialties()
    })

    return { from, specialty, specialties, nearest, findNearest, getAvailableBeds }
  }
}
</script>

<style scoped>
.page-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-height: 100vh;
  background-color: #e8e8e8;
  border-radius: 8px;
  padding: 2rem;
}

h2, h3, h4 {
  color: #2c3e50;
  margin-bottom: 2rem;
  text-align: center;
}

form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  max-width: 400px;
  width: 100%;
  background-color: #f0f0f0;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  margin-bottom: 2rem;
}

label {
  font-weight: bold;
  margin-bottom: 0.25rem;
}

.specialty-row {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.specialty-row label {
  margin-bottom: 0;
  white-space: nowrap;
}

.specialty-row select {
  width: 18.9rem;
}

.coords-row {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.coords-row label {
  margin-bottom: 0;
  white-space: nowrap;
}

.coords-inputs {
  display: flex;
  gap: 0.5rem;
}

.coords-inputs input {
  width: 10ch;
  flex-shrink: 0;
}

select, input {
  padding: 0.5rem;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 1rem;
  text-align: center;
}

button {
  padding: 0.75rem;
  background-color: #42b983;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.3s;
}

button:hover:not(:disabled) {
  background-color: #359268;
}

button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.result-card {
  background-color: #f0f0f0;
  border-left: 4px solid #42b983;
  padding: 1.5rem;
  border-radius: 8px;
  max-width: 500px;
  width: 100%;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  transform: translateX(-1.5rem);
}

.result-card h4 {
  margin-top: 0;
  color: #2c3e50;
  font-size: 1.5rem;
}

.result-card p {
  margin: 0.75rem 0;
  color: #555;
}

.result-card strong {
  color: #2c3e50;
}
</style>
