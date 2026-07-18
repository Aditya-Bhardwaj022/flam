document.addEventListener('DOMContentLoaded', () => {
    lucide.createIcons();

    const statPending = document.getElementById('stat-pending');
    const statRunning = document.getElementById('stat-running');
    const statCompleted = document.getElementById('stat-completed');
    const statFailed = document.getElementById('stat-failed');
    
    const workerStatusBadge = document.getElementById('worker-status-badge');
    const toggleWorkerBtn = document.getElementById('toggle-worker-btn');
    
    const enqueueForm = document.getElementById('enqueue-form');
    const jobIdInput = document.getElementById('job-id');
    const jobCmdInput = document.getElementById('job-cmd');
    
    const jobFilter = document.getElementById('job-filter');
    const jobsTbody = document.getElementById('jobs-tbody');

    const settingsForm = document.getElementById('settings-form');
    const configMaxRetries = document.getElementById('config-max-retries');
    const configBackoffBase = document.getElementById('config-backoff-base');
    
    let workersRunning = false;

    const getStateBadgeClasses = (state) => {
        const base = "inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold transition-colors focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2";
        switch (state) {
            case 'COMPLETED':
                return `${base} border-transparent bg-green-500 text-white`;
            case 'FAILED':
                return `${base} border-transparent bg-destructive text-destructive-foreground`;
            case 'RUNNING':
                return `${base} border-transparent bg-blue-500 text-white`;
            case 'PENDING':
                return `${base} border-transparent bg-secondary text-secondary-foreground`;
            default:
                return `${base} border-transparent bg-secondary text-secondary-foreground`;
        }
    };

    const fetchStatus = async () => {
        try {
            const res = await fetch('/api/status');
            const data = await res.json();
            
            const jobs = data.jobs || {};
            statPending.textContent = jobs.PENDING || 0;
            statRunning.textContent = jobs.RUNNING || 0;
            statCompleted.textContent = jobs.COMPLETED || 0;
            statFailed.textContent = jobs.FAILED || 0;
            
            if (document.activeElement !== configMaxRetries) {
                configMaxRetries.value = data.maxRetries || 3;
            }
            if (document.activeElement !== configBackoffBase) {
                configBackoffBase.value = data.backoffBase || 2;
            }

            workersRunning = data.workersRunning;
            
            if (workersRunning) {
                workerStatusBadge.className = "inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold transition-colors focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2 border-transparent bg-green-500 text-white";
                workerStatusBadge.innerHTML = '<i data-lucide="check-circle" class="h-3 w-3 mr-1"></i> Running';
                toggleWorkerBtn.innerHTML = '<i data-lucide="square" class="h-4 w-4 mr-2"></i> Stop Workers';
                toggleWorkerBtn.className = "inline-flex items-center justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 h-10 px-4 py-2 border border-input bg-background hover:bg-accent hover:text-accent-foreground";
            } else {
                workerStatusBadge.className = "inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold transition-colors focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2 border-transparent bg-destructive text-destructive-foreground";
                workerStatusBadge.innerHTML = '<i data-lucide="x-circle" class="h-3 w-3 mr-1"></i> Stopped';
                toggleWorkerBtn.innerHTML = '<i data-lucide="power" class="h-4 w-4 mr-2"></i> Start Workers';
                toggleWorkerBtn.className = "inline-flex items-center justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 h-10 px-4 py-2 bg-primary text-primary-foreground hover:bg-primary/90";
            }
            lucide.createIcons();
        } catch (err) {
            console.error("Failed to fetch status", err);
        }
    };

    const fetchJobs = async () => {
        try {
            const state = jobFilter.value;
            const url = state ? `/api/jobs?state=${state}` : '/api/jobs';
            const res = await fetch(url);
            const jobs = await res.json();
            
            jobsTbody.innerHTML = '';
            
            if (jobs.length === 0) {
                jobsTbody.innerHTML = `<tr class="border-b transition-colors hover:bg-muted/50"><td colspan="5" class="p-4 align-middle text-center text-sm text-muted-foreground">No jobs found.</td></tr>`;
                return;
            }
            
            jobs.forEach(job => {
                const tr = document.createElement('tr');
                tr.className = "border-b transition-colors hover:bg-muted/50 data-[state=selected]:bg-muted";
                
                const actions = job.state === 'FAILED' 
                    ? `<button class="inline-flex items-center justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 h-8 px-3 text-xs bg-secondary text-secondary-foreground hover:bg-secondary/80" onclick="retryJob('${job.id}')"><i data-lucide="refresh-cw" class="h-3 w-3 mr-1"></i> Retry</button>`
                    : '';
                
                tr.innerHTML = `
                    <td class="p-4 align-middle font-medium">${job.id}</td>
                    <td class="p-4 align-middle text-muted-foreground"><code class="relative rounded bg-muted px-[0.3rem] py-[0.2rem] font-mono text-sm font-semibold">${job.command}</code></td>
                    <td class="p-4 align-middle"><div class="${getStateBadgeClasses(job.state)}">${job.state}</div></td>
                    <td class="p-4 align-middle">${job.attempts || 0}</td>
                    <td class="p-4 align-middle">${actions}</td>
                `;
                jobsTbody.appendChild(tr);
            });
            lucide.createIcons();
        } catch (err) {
            console.error("Failed to fetch jobs", err);
        }
    };

    window.retryJob = async (id) => {
        try {
            await fetch(`/api/jobs/${id}/retry`, { method: 'POST' });
            fetchStatus();
            fetchJobs();
        } catch (err) {
            console.error("Failed to retry job", err);
        }
    };

    toggleWorkerBtn.addEventListener('click', async () => {
        try {
            const endpoint = workersRunning ? '/api/workers/stop' : '/api/workers/start?count=1';
            await fetch(endpoint, { method: 'POST' });
            workersRunning = !workersRunning;
            setTimeout(() => {
                fetchStatus();
            }, 500);
        } catch (err) {
            console.error("Failed to toggle workers", err);
        }
    });

    enqueueForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const id = jobIdInput.value.trim();
        const cmd = jobCmdInput.value.trim();
        if (!id || !cmd) return;
        
        try {
            const res = await fetch('/api/jobs', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ id, command: cmd })
            });
            if (res.ok) {
                jobIdInput.value = '';
                jobCmdInput.value = '';
                fetchStatus();
                fetchJobs();
            } else {
                alert("Failed to enqueue job");
            }
        } catch (err) {
            console.error("Error enqueuing job", err);
        }
    });

    settingsForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const maxRetries = configMaxRetries.value.trim();
        const backoffBase = configBackoffBase.value.trim();
        
        try {
            await fetch('/api/config', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ key: 'max-retries', value: maxRetries })
            });
            await fetch('/api/config', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ key: 'backoff-base', value: backoffBase })
            });
            
            fetchStatus();
            
            const btn = settingsForm.querySelector('button');
            const originalHTML = btn.innerHTML;
            btn.innerHTML = '<i data-lucide="check" class="h-4 w-4 mr-2"></i> Saved!';
            btn.classList.add('bg-green-500', 'text-white');
            btn.classList.remove('bg-secondary', 'text-secondary-foreground');
            lucide.createIcons();
            setTimeout(() => {
                btn.innerHTML = originalHTML;
                btn.classList.remove('bg-green-500', 'text-white');
                btn.classList.add('bg-secondary', 'text-secondary-foreground');
                lucide.createIcons();
            }, 2000);

        } catch (err) {
            console.error("Error updating config", err);
            alert("Failed to update config");
        }
    });

    jobFilter.addEventListener('change', fetchJobs);

    // Initial load
    fetchStatus();
    fetchJobs();
    
    // Poll every 3 seconds
    setInterval(() => {
        fetchStatus();
        fetchJobs();
    }, 3000);
});
