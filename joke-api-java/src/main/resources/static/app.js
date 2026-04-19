async function load() {
  const r = await fetch('api/all');
  const data = await r.json();
  const list = document.getElementById('list');
  const empty = document.getElementById('empty');
  const badge = document.getElementById('jokeCount');

  list.innerHTML = '';
  badge.textContent = `${data.length} joke${data.length !== 1 ? 's' : ''}`;

  if (data.length === 0) {
    empty.style.display = 'block';
    return;
  }
  empty.style.display = 'none';

  data.forEach((j, i) => {
    const li = document.createElement('li');
    li.className = 'joke-item';
    li.style.animationDelay = `${i * 35}ms`;
    li.innerHTML = `
      <span class="joke-num">#${j.id}</span>
      <span class="joke-text">${escapeHtml(j.text)}</span>
      <button class="del-btn" onclick="del(${j.id})" title="Delete">
        <i class="bi bi-x-lg"></i>
      </button>
    `;
    list.appendChild(li);
  });
}

function escapeHtml(str) {
  return str.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
}

async function addJoke() {
  const input = document.getElementById('jokeInput');
  const text = input.value.trim();
  if (!text) { showToast('Write a joke first 😅'); return; }

  await fetch('api/joke', { method: 'POST', body: new URLSearchParams({ text }) });
  input.value = '';
  showToast('Joke added! 🎉');
  load();
}

async function del(id) {
  await fetch('api/joke/' + id, { method: 'DELETE' });
  showToast('Deleted.');
  load();
}

async function clearAll() {
  if (!confirm('Delete ALL jokes? This cannot be undone.')) return;
  await fetch('api/clear', { method: 'DELETE' });
  showToast('All jokes cleared.');
  load();
}

async function importFile() {
  const f = document.getElementById('file').files[0];
  if (!f) { showToast('Choose a file first'); return; }

  const fd = new FormData();
  fd.append('file', f);
  await fetch('api/import', { method: 'POST', body: fd });
  document.getElementById('fileName').textContent = 'Choose file…';
  document.getElementById('file').value = '';
  showToast('Imported! 📥');
  load();
}

async function playAudio() {
  const btn = document.getElementById('audioBtn');
  btn.disabled = true;
  btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Loading…';

  try {
    const r = await fetch('api/joke/audio');
    if (!r.ok) throw new Error('Audio generation failed');
    const data = await r.json();
    const audio = new Audio(data.url);
    audio.play();
    audio.addEventListener('ended', () => {
      btn.disabled = false;
      btn.innerHTML = '<i class="bi bi-volume-up me-1"></i>Play';
    });
    audio.addEventListener('error', () => {
      showToast('Audio playback error.');
      btn.disabled = false;
      btn.innerHTML = '<i class="bi bi-volume-up me-1"></i>Play';
    });
  } catch {
    showToast('Could not load audio.');
    btn.disabled = false;
    btn.innerHTML = '<i class="bi bi-volume-up me-1"></i>Play';
  }
}

async function shuffleView() {
  const r = await fetch('api/joke');
  const data = await r.json();
  if (data.joke) showToast('🎲 ' + data.joke, 4000);
}

function updateFileName(input) {
  document.getElementById('fileName').textContent =
    input.files[0] ? input.files[0].name : 'Choose file…';
}

function showToast(msg, delay = 2500) {
  document.getElementById('toastMsg').textContent = msg;
  const toastEl = document.getElementById('liveToast');
  const toast = bootstrap.Toast.getOrCreateInstance(toastEl, { delay });
  toast.show();
}

document.addEventListener('DOMContentLoaded', () => {
  document.getElementById('jokeInput').addEventListener('keydown', e => {
    if (e.key === 'Enter') addJoke();
  });
  load();
});
