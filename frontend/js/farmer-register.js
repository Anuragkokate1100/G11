document.addEventListener('DOMContentLoaded',()=>{
 const form=document.getElementById('farmer-register-form'),msg=document.getElementById('message'),btn=document.getElementById('submit-btn');
 form.addEventListener('submit',async e=>{
  e.preventDefault();msg.className='alert d-none';msg.textContent='';
  const data=Object.fromEntries(new FormData(form).entries());
  if(data.password.length<8){msg.className='alert alert-danger';msg.textContent='Password must contain at least 8 characters.';return;}
  try{
   btn.disabled=true;btn.textContent='Creating account...';
   const res=await Api.post('/api/auth/farmer/register',data);
   if(!res.success||!res.data?.profile)throw new Error(res.message||'Registration failed');
   // Never store auth state on registration. Remove stale auth before returning to login.
   Api.clearAuth();
   msg.className='alert alert-success';msg.textContent='Account created successfully. Please log in with your email and password.';
   form.reset();
   const emailInput=document.querySelector('[name="email"]');
   const email=emailInput ? data.email : '';
   setTimeout(()=>{window.location.href='tractorlogin.html'+(email?'?role=farmer&email='+encodeURIComponent(email):'?role=farmer');},1000);
  }catch(err){msg.className='alert alert-danger';msg.textContent=err.message||'Could not register. Check that the backend is running.';}
  finally{btn.disabled=false;btn.textContent='Create Farmer Account';}
 });
});