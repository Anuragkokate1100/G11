document.addEventListener('DOMContentLoaded',()=>{
 const $=id=>document.getElementById(id);
 const localDate=d=>`${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`;
 const esc=v=>String(v??'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
 const data=async p=>{const r=await Api.get(p);return r?.data!==undefined?r.data:r;};
 const setMessage=(el,msg,ok=false)=>{el.className='small '+(ok?'text-success':'text-danger');el.textContent=msg;};
 const formDate=$('transaction-form')?.querySelector('[name="transactionDate"]');if(formDate)formDate.value=localDate(new Date());
 async function loadFinance(){
  const list=$('transaction-list'),summary=$('finance-summary');if(!list)return;
  try{
   const [rows,s]=await Promise.all([data('/api/farmer/transactions'),data('/api/farmer/transactions/summary')]);
   summary.innerHTML=`<strong>Selected period:</strong> ${esc(s.start)} – ${esc(s.end)}<br><span class="text-success">Income ${formatCurrency(s.income)}</span> · <span class="text-danger">Expenses ${formatCurrency(s.expenses)}</span> · <strong>Net ${formatCurrency(s.netBalance)}</strong>`;
   const monthStart=new Date();monthStart.setDate(1);const monthEnd=new Date();const qs='?start='+localDate(monthStart)+'&end='+localDate(monthEnd);
   const current=await data('/api/farmer/transactions/summary'+qs);
   $('expense-summary-card').textContent=formatCurrency(current.expenses);
   $('income-summary-card').textContent=formatCurrency(current.income);
   if(!rows.length){list.innerHTML='<p class="muted">No income or expense records yet.</p>';return;}
   list.innerHTML='<div class="table-responsive"><table class="table table-sm align-middle"><thead><tr><th>Date</th><th>Type / category</th><th>Amount</th><th></th></tr></thead><tbody>'+rows.map(r=>`<tr><td>${esc(r.transactionDate)}</td><td>${esc(r.transactionType)}<div class="muted">${esc(r.category)}${r.farmOrCrop?' · '+esc(r.farmOrCrop):''}</div></td><td>${formatCurrency(r.amount)}</td><td><button class="btn btn-sm btn-outline-danger" data-tx-delete="${r.id}" aria-label="Delete transaction">×</button></td></tr>`).join('')+'</tbody></table></div>';
   list.querySelectorAll('[data-tx-delete]').forEach(b=>b.onclick=async()=>{if(!confirm('Delete this income/expense record?'))return;try{await Api.delete('/api/farmer/transactions/'+b.dataset.txDelete);await loadFinance();await loadReports();}catch(e){setMessage(summary,e.message);}});
  }catch(e){list.textContent='Could not load farmer finance records: '+e.message;}
 }
 $('transaction-form')?.addEventListener('submit',async e=>{
  e.preventDefault();const f=e.currentTarget,d=Object.fromEntries(new FormData(f).entries());d.amount=Number(d.amount);d.farmOrCrop=d.farmOrCrop||null;d.description=d.description||null;
  try{await Api.post('/api/farmer/transactions',d);f.reset();if(formDate)formDate.value=localDate(new Date());await loadFinance();await loadReports();setMessage($('finance-summary'),'Record saved.',true);}catch(err){setMessage($('finance-summary'),err.message||'Could not save record.');}
 });
 async function loadReports(){
  const el=$('report-summary');if(!el)return;
  try{const r=await data('/api/farmer/reports/monthly');
   el.innerHTML=`<div><strong>${esc(r.start)} – ${esc(r.end)}</strong></div><div class="text-success">Income: ${formatCurrency(r.income)}</div><div class="text-danger">Expenses: ${formatCurrency(r.expenses)}</div><div><strong>Net: ${formatCurrency(r.netBalance)}</strong></div><hr><strong>Monthly breakdown</strong>`+(r.monthly||[]).map(m=>`<div class="border-top py-1"><strong>${esc(m.month)}</strong><div>Income ${formatCurrency(m.income)} · Costs ${formatCurrency(m.expenses)} · Net ${formatCurrency(m.netBalance)}</div></div>`).join('')||'<p class="muted">No records in this period yet.</p>';
  }catch(e){el.textContent='Could not load reports: '+e.message;}
 }
 $('print-report')?.addEventListener('click',()=>window.print());
 async function loadWeather(location=''){
  const result=$('weather-result');result.textContent='Loading current conditions and forecast…';
  try{
   const q=location.trim()?('?location='+encodeURIComponent(location.trim())):'';
   const w=await data('/api/farmer/weather'+q);const c=w.current||{},d=w.daily||{};
   const code=c.weather_code;
   result.innerHTML=`<div class="mb-2"><strong>${esc(w.location)}</strong><div class="muted">Updated ${esc(w.fetchedAt||'just now')} · ${esc(w.timezone||'local time')}</div></div><div class="row g-2"><div class="col-6"><div class="p-2 rounded bg-light"><div class="muted">Temperature</div><strong>${esc(c.temperature_2m??'—')} °C</strong></div></div><div class="col-6"><div class="p-2 rounded bg-light"><div class="muted">Humidity</div><strong>${esc(c.relative_humidity_2m??'—')}%</strong></div></div><div class="col-6"><div class="p-2 rounded bg-light"><div class="muted">Rain now</div><strong>${esc(c.rain??c.precipitation??'—')} mm</strong></div></div><div class="col-6"><div class="p-2 rounded bg-light"><div class="muted">Wind</div><strong>${esc(c.wind_speed_10m??'—')} km/h</strong></div></div></div><div class="mt-3"><strong>5-day forecast</strong>${(d.time||[]).map((day,i)=>`<div class="d-flex justify-content-between border-bottom py-1"><span>${esc(day)}</span><span>${esc(d.temperature_2m_min?.[i]??'—')}–${esc(d.temperature_2m_max?.[i]??'—')} °C · rain chance ${esc(d.precipitation_probability_max?.[i]??'—')}%</span></div>`).join('')}</div>`;
  }catch(e){result.textContent=e.message||'Weather is unavailable. Try a nearby town.';}
 }
 $('weather-form')?.addEventListener('submit',e=>{e.preventDefault();loadWeather($('weather-location').value);});
 async function askAi(question){
  const history=$('chat-history'),btn=$('ai-send');btn.disabled=true;
  const user=document.createElement('p');user.className='mb-2';user.textContent='You: '+question;history.appendChild(user);
  const answer=document.createElement('p');answer.className='mb-2';answer.textContent='Assistant: Thinking…';history.appendChild(answer);
  try{const r=await Api.post('/api/farmer/ai/chat',{message:question});answer.textContent='Assistant: '+(r.reply||r.data?.reply||'No response received.');}
  catch(e){answer.textContent='Assistant: '+(e.message||'The AI service is unavailable.');}
  finally{btn.disabled=false;history.scrollTop=history.scrollHeight;}
 }
 $('ai-form')?.addEventListener('submit',e=>{e.preventDefault();const q=$('ai-question').value.trim();if(!q)return;$('ai-question').value='';askAi(q);});
 async function loadServices(){
  const el=$('service-list');try{const rows=await data('/api/farmer/service-requests');if(!rows.length){el.textContent='No service requests yet.';return;}
   el.innerHTML=rows.map(r=>`<div class="border-top py-2"><strong>${esc(r.serviceRequired)}</strong> · <span class="badge text-bg-light">${esc(r.status)}</span><div>${esc(r.location)}${r.preferredDate?' · '+esc(r.preferredDate):''}</div><div class="muted">${esc(r.details||'')}</div>${r.status==='PENDING'?`<button class="btn btn-sm btn-outline-danger mt-1" data-cancel-request="${r.id}">Cancel request</button>`:''}</div>`).join('');
   el.querySelectorAll('[data-cancel-request]').forEach(b=>b.onclick=async()=>{if(!confirm('Cancel this pending request?'))return;try{await Api.post('/api/farmer/service-requests/'+b.dataset.cancelRequest+'/cancel',{});await loadServices();await loadNotifications();}catch(e){el.textContent=e.message;}});
  }catch(e){el.textContent='Could not load service requests: '+e.message;}
 }
 $('service-form')?.addEventListener('submit',async e=>{e.preventDefault();const f=e.currentTarget,d=Object.fromEntries(new FormData(f).entries());d.preferredDate=d.preferredDate||null;d.details=d.details||null;
  const btn=f.querySelector('button[type=submit]');btn.disabled=true;try{await Api.post('/api/farmer/service-requests',d);f.reset();await loadServices();await loadNotifications();}catch(err){alert(err.message||'Could not submit request.');}finally{btn.disabled=false;}
 });
 async function loadBills(){
  const el=$('farmer-bills-list');if(!el)return;
  try{const rows=await data('/api/farmer/bills');if(!rows.length){el.textContent='No bills have been linked to your account yet.';return;}
   el.innerHTML=rows.map(b=>`<div class="border-top py-2"><strong>Bill #${esc(b.billNumber)}</strong><div>${esc(b.ownerName)}${b.tractorNumber?' · '+esc(b.tractorNumber):''}</div><div>${esc(b.serviceDetails)}</div><div>${esc(b.billDate)} · <strong>${formatCurrency(b.amount)}</strong></div><div class="muted">${esc(b.paymentStatus)}</div><button class="btn btn-sm btn-outline-secondary mt-1" data-print-bill="${esc(b.billNumber)}">Print bill details</button></div>`).join('');
   el.querySelectorAll('[data-print-bill]').forEach(b=>b.onclick=()=>window.print());
  }catch(e){el.textContent='Could not load bills: '+e.message;}
 }
 async function loadNotifications(){
  const el=$('notification-list');if(!el)return;try{const n=await data('/api/farmer/notifications');$('read-all-notifications').textContent='Mark all read ('+(n.unreadCount||0)+')';
   if(notificationLink){let badge=notificationLink.querySelector('.unread-count');if(!badge){badge=document.createElement('span');badge.className='unread-count badge rounded-pill text-bg-warning ms-2';notificationLink.appendChild(badge);}badge.textContent=n.unreadCount||0;badge.classList.toggle('d-none',!(n.unreadCount>0));}
   if(!n.items?.length){el.textContent='No notifications yet.';return;}
   el.innerHTML=n.items.map(x=>`<div class="border-top py-2 ${x.read?'opacity-75':'fw-semibold'}"><div>${esc(x.title)} ${x.read?'':'<span class="badge text-bg-success">New</span>'}</div><div class="fw-normal">${esc(x.message)}</div><div class="muted fw-normal">${esc(x.createdAt||'')}</div>${x.read?'':`<button class="btn btn-sm btn-link p-0" data-read-notification="${x.id}">Mark read</button>`}</div>`).join('');
   el.querySelectorAll('[data-read-notification]').forEach(b=>b.onclick=async()=>{try{await Api.request('/api/farmer/notifications/'+b.dataset.readNotification+'/read',{method:'PATCH'});await loadNotifications();}catch(e){el.textContent=e.message;}});
  }catch(e){el.textContent='Could not load notifications: '+e.message;}
 }
 $('read-all-notifications')?.addEventListener('click',async()=>{try{await Api.post('/api/farmer/notifications/read-all',{});await loadNotifications();}catch(e){alert(e.message);}});
 // Correct API error handling preserves role state for the current page; a 401 still redirects.
 const notificationLink=document.querySelector('.nav-link[href="#notifications"]');
 loadFinance();loadReports();loadWeather();loadServices();loadBills();loadNotifications();
});