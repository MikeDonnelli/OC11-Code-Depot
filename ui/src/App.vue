<template>
  <div class="main">
    <h1>OC11 — Hospital Finder (POC)</h1>
    <HospitalSearch />
  </div>
</template>

<script>
import HospitalSearch from './components/HospitalSearch.vue'
export default {
  components: { HospitalSearch }
}
</script>

<style scoped>

h1 {
  color: #2c3e50;
  margin-bottom: 2rem;
  text-align: center;
}

</style>