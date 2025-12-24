export async function findNearest(payload) {
  const res = await fetch('/api/hospitals/nearest', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  })
  if (!res.ok) throw new Error('Failed to find nearest')
  return res.json()
}
