import { mount } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { nextTick } from 'vue'
import HospitalSearch from '../HospitalSearch.vue'

// Helper pour attendre les promesses
const flushPromises = () => new Promise(resolve => setTimeout(resolve, 0))

describe('HospitalSearch', () => {
  let fetchMock

  beforeEach(() => {
    fetchMock = vi.fn()
    global.fetch = fetchMock
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('Rendu initial', () => {
    it('affiche le titre et le formulaire', () => {
      fetchMock.mockResolvedValue({ ok: true, json: () => Promise.resolve([]) })
      const wrapper = mount(HospitalSearch)
      
      expect(wrapper.text()).toContain('Trouver l\'hôpital le plus proche')
      expect(wrapper.find('select').exists()).toBe(true)
      expect(wrapper.find('button').exists()).toBe(true)
    })

    it('désactive le bouton sans spécialité sélectionnée', () => {
      fetchMock.mockResolvedValue({ ok: true, json: () => Promise.resolve([]) })
      const wrapper = mount(HospitalSearch)
      
      expect(wrapper.find('button').attributes('disabled')).toBeDefined()
    })

    it('initialise les coordonnées par défaut', () => {
      fetchMock.mockResolvedValue({ ok: true, json: () => Promise.resolve([]) })
      const wrapper = mount(HospitalSearch)
      
      const inputs = wrapper.findAll('input')
      expect(inputs[0].element.value).toBe('48.85')
      expect(inputs[1].element.value).toBe('2.35')
    })
  })

  describe('Chargement des spécialités', () => {
    it('charge les spécialités au montage', async () => {
      const specialties = ['Cardiologie', 'Neurologie', 'Pédiatrie']
      fetchMock.mockResolvedValue({ ok: true, json: () => Promise.resolve(specialties) })
      
      const wrapper = mount(HospitalSearch)
      await flushPromises()
      
      const options = wrapper.findAll('option')
      expect(options).toHaveLength(specialties.length + 1) // +1 pour l'option par défaut
      expect(options[1].text()).toBe('Cardiologie')
      expect(options[2].text()).toBe('Neurologie')
      expect(options[3].text()).toBe('Pédiatrie')
    })

    it('gère l\'erreur de chargement des spécialités', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      fetchMock.mockRejectedValue(new Error('Network error'))
      
      const wrapper = mount(HospitalSearch)
      await flushPromises()
      
      expect(consoleErrorSpy).toHaveBeenCalledWith('Failed to load specialties:', expect.any(Error))
      const options = wrapper.findAll('option')
      expect(options).toHaveLength(1) // Seulement l'option par défaut
      
      consoleErrorSpy.mockRestore()
    })

    it('affiche une liste vide si l\'API retourne un tableau vide', async () => {
      fetchMock.mockResolvedValue({ ok: true, json: () => Promise.resolve([]) })
      
      const wrapper = mount(HospitalSearch)
      await flushPromises()
      
      const options = wrapper.findAll('option')
      expect(options).toHaveLength(1) // Seulement l'option par défaut
    })
  })

  describe('Interaction utilisateur', () => {
    it('active le bouton après sélection d\'une spécialité', async () => {
      fetchMock.mockResolvedValue({ ok: true, json: () => Promise.resolve(['Cardiologie']) })
      
      const wrapper = mount(HospitalSearch)
      await flushPromises()
      
      const select = wrapper.find('select')
      await select.setValue('Cardiologie')
      
      expect(wrapper.find('button').attributes('disabled')).toBeUndefined()
    })

    it('met à jour les coordonnées lors de la saisie', async () => {
      fetchMock.mockResolvedValue({ ok: true, json: () => Promise.resolve([]) })
      
      const wrapper = mount(HospitalSearch)
      const inputs = wrapper.findAll('input')
      
      await inputs[0].setValue('45.5')
      await inputs[1].setValue('3.25')
      
      expect(inputs[0].element.value).toBe('45.5')
      expect(inputs[1].element.value).toBe('3.25')
    })
  })

  describe('Recherche d\'hôpital', () => {
    it('affiche le résultat après une recherche réussie', async () => {
      const specialties = ['Cardiologie']
      const hospitals = [
        { id: 1, name: 'Hôpital A', lat: 48.856, lon: 2.352, specialties: [{ specialty: 'Cardiologie', availableBeds: 5 }] }
      ]
      const nearestResponse = {
        hospital: hospitals[0],
        distanceKm: 2.5,
        duration: '8m 30s'
      }

      fetchMock
        .mockResolvedValueOnce({ ok: true, json: () => Promise.resolve(specialties) })
        .mockResolvedValueOnce({ ok: true, json: () => Promise.resolve(hospitals) })
        .mockResolvedValueOnce({ ok: true, json: () => Promise.resolve(nearestResponse) })

      const wrapper = mount(HospitalSearch)
      await flushPromises()

      await wrapper.find('select').setValue('Cardiologie')
      await wrapper.find('form').trigger('submit')
      await flushPromises()

      expect(wrapper.text()).toContain('Hôpital le plus proche')
      expect(wrapper.text()).toContain('Hôpital A')
      expect(wrapper.text()).toContain('2.5 km')
      expect(wrapper.text()).toContain('8m 30s')
    })

    it('conserve la spécialité recherchée même après changement du select', async () => {
      const specialties = ['Cardiologie', 'Neurologie']
      const hospitals = [
        { id: 1, name: 'Hôpital A', lat: 48.856, lon: 2.352, specialties: [{ specialty: 'Cardiologie', availableBeds: 5 }] }
      ]
      const nearestResponse = {
        hospital: hospitals[0],
        distanceKm: 2.5,
        duration: '8m 30s'
      }

      fetchMock
        .mockResolvedValueOnce({ ok: true, json: () => Promise.resolve(specialties) })
        .mockResolvedValueOnce({ ok: true, json: () => Promise.resolve(hospitals) })
        .mockResolvedValueOnce({ ok: true, json: () => Promise.resolve(nearestResponse) })

      const wrapper = mount(HospitalSearch)
      await flushPromises()

      await wrapper.find('select').setValue('Cardiologie')
      await wrapper.find('form').trigger('submit')
      await flushPromises()

      const resultText = wrapper.text()
      expect(resultText).toContain('Cardiologie')

      // Changement de la sélection
      await wrapper.find('select').setValue('Neurologie')
      
      // La spécialité affichée dans le résultat doit rester "Cardiologie"
      expect(wrapper.text()).toContain('Cardiologie')
    })

    it('affiche le nombre de lits disponibles pour la spécialité recherchée', async () => {
      const specialties = ['Cardiologie']
      const hospitals = [
        { id: 1, name: 'Hôpital A', lat: 48.856, lon: 2.352, specialties: [
          { specialty: 'Cardiologie', availableBeds: 7 },
          { specialty: 'Neurologie', availableBeds: 3 }
        ]}
      ]
      const nearestResponse = {
        hospital: hospitals[0],
        distanceKm: 2.5,
        duration: '8m 30s'
      }

      fetchMock
        .mockResolvedValueOnce({ ok: true, json: () => Promise.resolve(specialties) })
        .mockResolvedValueOnce({ ok: true, json: () => Promise.resolve(hospitals) })
        .mockResolvedValueOnce({ ok: true, json: () => Promise.resolve(nearestResponse) })

      const wrapper = mount(HospitalSearch)
      await flushPromises()

      await wrapper.find('select').setValue('Cardiologie')
      await wrapper.find('form').trigger('submit')
      await flushPromises()

      expect(wrapper.text()).toContain('Lits disponibles : 7')
    })
  })

  describe('Calcul des lits disponibles', () => {
    it('retourne le nombre de lits pour la spécialité correspondante', async () => {
      fetchMock.mockResolvedValue({ ok: true, json: () => Promise.resolve([]) })
      const wrapper = mount(HospitalSearch)
      
      const hospital = {
        specialties: [
          { specialty: 'Cardiologie', availableBeds: 5 },
          { specialty: 'Neurologie', availableBeds: 3 }
        ]
      }
      
      const beds = wrapper.vm.getAvailableBeds(hospital, 'Cardiologie')
      expect(beds).toBe(5)
    })

    it('retourne 0 si la spécialité n\'existe pas', async () => {
      fetchMock.mockResolvedValue({ ok: true, json: () => Promise.resolve([]) })
      const wrapper = mount(HospitalSearch)
      
      const hospital = {
        specialties: [
          { specialty: 'Cardiologie', availableBeds: 5 }
        ]
      }
      
      const beds = wrapper.vm.getAvailableBeds(hospital, 'Pédiatrie')
      expect(beds).toBe(0)
    })

    it('retourne 0 si l\'hôpital n\'a pas de spécialités', async () => {
      fetchMock.mockResolvedValue({ ok: true, json: () => Promise.resolve([]) })
      const wrapper = mount(HospitalSearch)
      
      const hospital = { specialties: null }
      
      const beds = wrapper.vm.getAvailableBeds(hospital, 'Cardiologie')
      expect(beds).toBe(0)
    })

    it('gère la casse de manière insensible', async () => {
      fetchMock.mockResolvedValue({ ok: true, json: () => Promise.resolve([]) })
      const wrapper = mount(HospitalSearch)
      
      const hospital = {
        specialties: [
          { specialty: 'Cardiologie', availableBeds: 5 }
        ]
      }
      
      const beds = wrapper.vm.getAvailableBeds(hospital, 'CARDIOLOGIE')
      expect(beds).toBe(5)
    })
  })

  describe('Tests négatifs - Gestion des erreurs', () => {
    it('gère l\'échec de la recherche d\'hôpitaux', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      fetchMock
        .mockResolvedValueOnce({ ok: true, json: () => Promise.resolve(['Cardiologie']) })
        .mockResolvedValueOnce({ ok: true, json: () => Promise.resolve([]) }) // Liste d'hôpitaux vide

      const wrapper = mount(HospitalSearch)
      await flushPromises()

      await wrapper.find('select').setValue('Cardiologie')
      
      await wrapper.find('form').trigger('submit')
      await flushPromises()

      // Vérifie qu'aucun résultat n'est affiché car la liste d'hôpitaux est vide
      expect(wrapper.find('.result-card').exists()).toBe(false)
      
      consoleErrorSpy.mockRestore()
    })

    it('gère les coordonnées invalides', async () => {
      fetchMock.mockResolvedValue({ ok: true, json: () => Promise.resolve(['Cardiologie']) })
      
      const wrapper = mount(HospitalSearch)
      await flushPromises()

      const inputs = wrapper.findAll('input')
      await inputs[0].setValue('invalid')
      await inputs[1].setValue('invalid')
      
      // Les champs de saisie contiennent la chaîne "invalid"
      expect(inputs[0].element.value).toBe('invalid')
      expect(inputs[1].element.value).toBe('invalid')
    })

    it('ne soumet pas le formulaire sans spécialité', async () => {
      const specialties = ['Cardiologie']
      fetchMock.mockResolvedValue({ ok: true, json: () => Promise.resolve(specialties) })
      
      const wrapper = mount(HospitalSearch)
      await flushPromises()

      // Tentative de soumission sans sélectionner de spécialité
      const button = wrapper.find('button')
      expect(button.attributes('disabled')).toBeDefined()
    })

    it('gère l\'erreur réseau lors de la recherche', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      fetchMock
        .mockResolvedValueOnce({ ok: true, json: () => Promise.resolve(['Cardiologie']) })
        .mockRejectedValueOnce(new Error('Network error'))

      const wrapper = mount(HospitalSearch)
      await flushPromises()

      await wrapper.find('select').setValue('Cardiologie')
      await wrapper.find('form').trigger('submit')
      await flushPromises()

      // L'erreur devrait être gérée sans planter l'application
      expect(wrapper.find('.result-card').exists()).toBe(false)
      
      consoleErrorSpy.mockRestore()
    })

    it('affiche 0 lits si la spécialité n\'a pas de lits disponibles', async () => {
      const specialties = ['Cardiologie']
      const hospitals = [
        { id: 1, name: 'Hôpital A', lat: 48.856, lon: 2.352, specialties: [
          { specialty: 'Cardiologie', availableBeds: 0 }
        ]}
      ]
      const nearestResponse = {
        hospital: hospitals[0],
        distanceKm: 2.5,
        duration: '8m 30s'
      }

      fetchMock
        .mockResolvedValueOnce({ ok: true, json: () => Promise.resolve(specialties) })
        .mockResolvedValueOnce({ ok: true, json: () => Promise.resolve(hospitals) })
        .mockResolvedValueOnce({ ok: true, json: () => Promise.resolve(nearestResponse) })

      const wrapper = mount(HospitalSearch)
      await flushPromises()

      await wrapper.find('select').setValue('Cardiologie')
      await wrapper.find('form').trigger('submit')
      await flushPromises()

      expect(wrapper.text()).toContain('Lits disponibles : 0')
    })
  })
})

