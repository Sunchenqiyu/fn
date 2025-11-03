const timelineData = [
  {
    title: '交付客户演示稿',
    owner: 'Mila',
    due: '今天 16:00',
    priority: 'high',
  },
  {
    title: '移动端交互验收',
    owner: 'Hank',
    due: '今天 19:30',
    priority: 'medium',
  },
  {
    title: '设计系统整理',
    owner: 'Joyce',
    due: '周三 10:00',
    priority: 'low',
  },
];

const membersData = [
  { name: 'Mila Zhang', role: '产品经理', status: '在线' },
  { name: 'Hank Li', role: '前端开发', status: '忙碌' },
  { name: 'Joyce Wu', role: '视觉设计', status: '在线' },
  { name: 'Kevin Chen', role: '后端开发', status: '离线' },
];

const notes = [];

function renderTimeline() {
  const container = document.querySelector('#timeline');
  const template = document.querySelector('#timeline-item-template');
  container.innerHTML = '';

  timelineData
    .slice()
    .sort((a, b) => priorityRank(a.priority) - priorityRank(b.priority))
    .forEach((item) => {
      const node = template.content.cloneNode(true);
      const badge = node.querySelector('[data-priority]');
      const title = node.querySelector('[data-title]');
      const meta = node.querySelector('[data-meta]');

      badge.style.background = priorityColor(item.priority);
      badge.title = `优先级：${priorityLabel(item.priority)}`;
      title.textContent = item.title;
      meta.textContent = `${item.owner} · ${item.due}`;

      container.appendChild(node);
    });
}

function renderMembers() {
  const container = document.querySelector('#members');
  const template = document.querySelector('#member-item-template');
  container.innerHTML = '';

  membersData.forEach((member) => {
    const node = template.content.cloneNode(true);
    node.querySelector('[data-initials]').textContent = getInitials(member.name);
    node.querySelector('[data-name]').textContent = member.name;
    node.querySelector('[data-role]').textContent = member.role;
    node.querySelector('[data-status]').textContent = member.status;
    container.appendChild(node);
  });
}

function renderNotes() {
  const list = document.querySelector('#notes-list');
  const template = document.querySelector('#note-item-template');
  list.innerHTML = '';

  notes.forEach((note) => {
    const node = template.content.cloneNode(true);
    node.querySelector('[data-text]').textContent = note.text;
    node.querySelector('[data-time]').textContent = note.time;
    list.prepend(node);
  });
}

function priorityRank(priority) {
  return { high: 0, medium: 1, low: 2 }[priority] ?? 3;
}

function priorityColor(priority) {
  return {
    high: 'linear-gradient(135deg, #ef4444, #f97316)',
    medium: 'linear-gradient(135deg, #f59e0b, #fbbf24)',
    low: 'linear-gradient(135deg, #22c55e, #10b981)',
  }[priority];
}

function priorityLabel(priority) {
  return { high: '高', medium: '中', low: '低' }[priority] ?? '未知';
}

function getInitials(name) {
  return name
    .split(' ')
    .map((part) => part.charAt(0).toUpperCase())
    .join('');
}

function toggleTheme() {
  document.body.classList.toggle('dark');
}

function openModal() {
  const modal = document.querySelector('#task-modal');
  if (typeof modal.showModal === 'function') {
    modal.showModal();
  }
}

function closeModal() {
  const modal = document.querySelector('#task-modal');
  modal.close();
}

function registerModal() {
  const modal = document.querySelector('#task-modal');
  const form = document.querySelector('#task-form');

  form.addEventListener('submit', (event) => {
    event.preventDefault();
    const formData = new FormData(form);

    const newTask = {
      title: formData.get('title'),
      owner: formData.get('owner'),
      due: '即将安排',
      priority: formData.get('priority'),
    };

    timelineData.push(newTask);
    renderTimeline();
    form.reset();
    closeModal();
  });

  modal.addEventListener('close', () => {
    form.reset();
  });

  modal.addEventListener('cancel', (event) => {
    event.preventDefault();
    closeModal();
  });
}

function registerNotes() {
  const form = document.querySelector('#notes-form');
  const textarea = document.querySelector('#notes-input');

  form.addEventListener('submit', (event) => {
    event.preventDefault();
    const text = textarea.value.trim();
    if (!text) return;

    notes.unshift({
      text,
      time: new Intl.DateTimeFormat('zh-CN', {
        hour: '2-digit',
        minute: '2-digit',
      }).format(new Date()),
    });

    textarea.value = '';
    renderNotes();
  });
}

function registerNav() {
  const buttons = document.querySelectorAll('.nav__item');
  buttons.forEach((button) => {
    button.addEventListener('click', () => {
      buttons.forEach((item) => item.classList.remove('nav__item--active'));
      button.classList.add('nav__item--active');
    });
  });
}

function registerThemeToggle() {
  const toggle = document.querySelector('.theme-toggle');
  toggle.addEventListener('click', () => {
    toggleTheme();
    const isDark = document.body.classList.contains('dark');
    toggle.querySelector('span:last-child').textContent = isDark ? '切换为亮色' : '切换主题';
  });
}

function registerTaskButton() {
  const button = document.querySelector('#new-task');
  button.addEventListener('click', openModal);
}

function init() {
  renderTimeline();
  renderMembers();
  renderNotes();
  registerModal();
  registerNotes();
  registerNav();
  registerThemeToggle();
  registerTaskButton();
}

window.addEventListener('DOMContentLoaded', init);
