/**
 * 智能安装助手 - 前端交互逻辑
 * 支持 SSE 流式输出、Markdown 渲染
 */

let sessionId = null;
let currentAssistantBubble = null;
const chatBox = document.getElementById('chat-box');
const textarea = document.getElementById('msg-input');
const btn = document.getElementById('send-btn');
const statusEl = document.getElementById('api-status');
const sessionEl = document.getElementById('session-id');
const elapsedEl = document.getElementById('elapsed-ms');

// ===== Event Listeners =====
textarea.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); send(); }
});
textarea.addEventListener('input', () => {
    textarea.style.height = 'auto';
    textarea.style.height = Math.min(textarea.scrollHeight, 120) + 'px';
});

// ===== Main Send =====
async function send() {
    const msg = textarea.value.trim();
    if (!msg) return;

    appendMsg('user', msg);
    clearInput();
    setStatus('thinking', '思考中...');

    // 创建空的 assistant bubble 用于流式填充
    currentAssistantBubble = createAssistantBubble();
    const t0 = performance.now();

    try {
        const resp = await fetch('/api/chat/sync', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ message: msg, sessionId: sessionId })
        });

        if (!resp.ok) throw new Error('HTTP ' + resp.status);
        const data = await resp.json();

        // 更新流式 bubble 为最终内容
        if (currentAssistantBubble) {
            currentAssistantBubble.innerHTML = markdownToHtml(data.response);
            currentAssistantBubble.classList.remove('typing');
        } else {
            appendMsg('assistant', data.response);
        }

        // 更新会话
        if (!sessionId) sessionId = await getLatestSessionId();
        setStatus('online', '就绪');
        sessionEl.textContent = sessionId || '-';
        elapsedEl.textContent = Math.round(performance.now() - t0) + 'ms';

    } catch (e) {
        if (currentAssistantBubble) {
            currentAssistantBubble.innerHTML = '<span style="color:#f87171">请求失败: ' + escapeHtml(e.message) + '</span>';
        } else {
            appendMsg('system', '请求失败: ' + e.message);
        }
        setStatus('offline', '错误');
    }
    currentAssistantBubble = null;
    btn.disabled = false;
    textarea.focus();
}

// ===== Quick Send =====
function sendQuick(msg) {
    textarea.value = msg;
    send();
}

// ===== Session Management =====
async function newSession() {
    sessionId = null;
    sessionEl.textContent = '-';
    chatBox.innerHTML = '';
    appendMsg('assistant', '你好！我是 **智能安装助手**，可以帮你完成集群创建、微服务管理、安装指引和故障诊断。\n\n试试点击上面的快捷按钮 👆');
}

async function getLatestSessionId() {
    try {
        const resp = await fetch('/api/sessions');
        const sessions = await resp.json();
        return sessions.length > 0 ? sessions[0].id : null;
    } catch { return null; }
}

// ===== Append Message =====
function appendMsg(role, text) {
    const wrapper = document.createElement('div');
    wrapper.className = 'msg ' + role;

    // Avatar
    const avatar = document.createElement('div');
    avatar.className = 'avatar';
    avatar.textContent = role === 'user' ? '👤' : role === 'system' ? '⚠️' : '🤖';
    wrapper.appendChild(avatar);

    // Bubble
    const bubble = document.createElement('div');
    bubble.className = 'bubble';
    bubble.innerHTML = role === 'assistant' ? markdownToHtml(text) : escapeHtml(text);
    wrapper.appendChild(bubble);

    chatBox.appendChild(wrapper);
    chatBox.scrollTop = chatBox.scrollHeight;
    return bubble;
}

function createAssistantBubble() {
    const wrapper = document.createElement('div');
    wrapper.className = 'msg assistant';

    const avatar = document.createElement('div');
    avatar.className = 'avatar';
    avatar.textContent = '🤖';
    wrapper.appendChild(avatar);

    const bubble = document.createElement('div');
    bubble.className = 'bubble';
    bubble.innerHTML = '<span class="typing-indicator"><span></span><span></span><span></span></span>';
    wrapper.appendChild(bubble);

    chatBox.appendChild(wrapper);
    chatBox.scrollTop = chatBox.scrollHeight;
    return bubble;
}

// ===== Status =====
function setStatus(state, text) {
    statusEl.innerHTML = '<span class="dot ' + state + '"></span>' + text;
}

function clearInput() {
    textarea.value = '';
    textarea.style.height = 'auto';
    btn.disabled = true;
}

// ===== Markdown → HTML =====
function markdownToHtml(text) {
    let html = escapeHtml(text);

    // 代码块 (```...```)
    html = html.replace(/```(\w*)\n?([\s\S]*?)```/g, '<pre><code>$2</code></pre>');

    // 行内代码 (`...`)
    html = html.replace(/`([^`]+)`/g, '<code>$1</code>');

    // 粗体 (**...**)
    html = html.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');

    // 斜体 (*...*)
    html = html.replace(/\*([^*]+)\*/g, '<em>$1</em>');

    // 三级标题
    html = html.replace(/^### (.+)$/gm, '<h3>$1</h3>');

    // 二级标题
    html = html.replace(/^## (.+)$/gm, '<strong style="font-size:16px;color:#38bdf8;display:block;margin-top:12px;">$1</strong>');

    // 无序列表 (- ...)
    html = html.replace(/^[-*] (.+)$/gm, '<li>$1</li>');
    html = replaceBlocks(html, /(<li>.*?<\/li>\n?)+/g, '<ul>$&</ul>');

    // 有序列表 (1. ...)
    html = html.replace(/^\d+\. (.+)$/gm, '<li>$1</li>');
    html = html.replace(/((?:<li>.*?<\/li>\n?)+)/g, (m) => m.includes('<ul>') ? m : '<ol>' + m + '</ol>');

    // 水平线
    html = html.replace(/^---+$/gm, '<hr>');

    // 表格 (简单支持)
    html = html.replace(/^\|(.+)\|$/gm, (match) => {
        const cells = match.split('|').filter(c => c.trim());
        const isHeader = cells.some(c => /^[-:]+$/.test(c.trim()));
        if (isHeader) return '';
        const tag = match.includes('---') ? '' : 'td';
        return '<tr>' + cells.map(c => '<td>' + c.trim() + '</td>').join('') + '</tr>';
    });

    // 换行
    html = html.replace(/\n/g, '<br>');

    // 清理空 <br> 在块元素后
    html = html.replace(/(<\/pre>|<\/ul>|<\/ol>|<\/h3>|<\/tr>|<hr>)\s*<br>/g, '$1');

    return html;
}

function replaceBlocks(html, regex, template) {
    return html.replace(regex, (m) => template.replace('$&', m));
}

function escapeHtml(str) {
    return str
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;');
}

// ===== SSE 流式聊天 (备用，Phase 2 使用) =====
async function sendStream(msg) {
    appendMsg('user', msg);
    clearInput();
    setStatus('thinking', '思考中...');
    currentAssistantBubble = createAssistantBubble();
    const t0 = performance.now();
    let fullText = '';

    try {
        const resp = await fetch('/api/chat', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ message: msg, sessionId: sessionId })
        });

        const reader = resp.body.getReader();
        const decoder = new TextDecoder();
        let buffer = '';

        while (true) {
            const { done, value } = await reader.read();
            if (done) break;
            buffer += decoder.decode(value, { stream: true });

            // 处理 SSE 事件
            const lines = buffer.split('\n');
            buffer = lines.pop(); // 最后一个不完整行保留

            for (const line of lines) {
                if (line.startsWith('event: session')) continue; // 跳过 session 事件
                if (line.startsWith('data: ')) {
                    const chunk = line.slice(6);
                    if (chunk === '0') continue; // 跳过初始 session id
                    fullText += chunk;
                    if (currentAssistantBubble) {
                        currentAssistantBubble.innerHTML = markdownToHtml(fullText);
                        chatBox.scrollTop = chatBox.scrollHeight;
                    }
                }
            }
        }

        if (currentAssistantBubble) {
            currentAssistantBubble.innerHTML = markdownToHtml(fullText);
        }
        setStatus('online', '就绪');
        if (!sessionId) sessionId = await getLatestSessionId();
        sessionEl.textContent = sessionId || '-';
        elapsedEl.textContent = Math.round(performance.now() - t0) + 'ms';

    } catch (e) {
        if (currentAssistantBubble) {
            currentAssistantBubble.innerHTML = '<span style="color:#f87171">连接失败: ' + escapeHtml(e.message) + '</span>';
        }
        setStatus('offline', '错误');
    }
    currentAssistantBubble = null;
    btn.disabled = false;
    textarea.focus();
}

// ===== Init =====
document.addEventListener('DOMContentLoaded', () => {
    textarea.focus();
});
