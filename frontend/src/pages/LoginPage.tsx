import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import toast from 'react-hot-toast';
import { Compass, Mail, Lock, AlertCircle } from 'lucide-react';

const EMAIL_REGEX = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [errors, setErrors] = useState<{ email?: string; password?: string }>({});
  const [touched, setTouched] = useState<Record<string, boolean>>({});
  const { login, isLoading } = useAuthStore();
  const navigate = useNavigate();

  const validateEmail = (value: string): string | undefined => {
    if (!value.trim()) return 'Email address is required.';
    if (!EMAIL_REGEX.test(value.trim())) return 'Please enter a valid email (e.g., john@example.com).';
    return undefined;
  };

  const validatePassword = (value: string): string | undefined => {
    if (!value) return 'Password is required.';
    if (value.length < 6) return 'Password must be at least 6 characters.';
    return undefined;
  };

  const handleBlur = (field: string) => {
    setTouched(prev => ({ ...prev, [field]: true }));
    const newErrors = { ...errors };
    if (field === 'email') newErrors.email = validateEmail(email);
    if (field === 'password') newErrors.password = validatePassword(password);
    setErrors(newErrors);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setTouched({ email: true, password: true });

    const emailErr = validateEmail(email);
    const passwordErr = validatePassword(password);
    setErrors({ email: emailErr, password: passwordErr });

    if (emailErr || passwordErr) {
      toast.error('Please fix the errors below before signing in.');
      return;
    }

    try {
      await login({ email: email.trim(), password });
      toast.success('Welcome back!');
      navigate('/dashboard');
    } catch (err: any) {
      toast.error(err.message || 'Invalid email or password. Please try again.');
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card animate-in">
        <div style={{ display: 'flex', justifyContent: 'center', marginBottom: '16px' }}>
          <Compass size={48} style={{ color: '#6366f1' }} />
        </div>
        <h1>VoyageCraft</h1>
        <p className="auth-subtitle">Sign in to plan your next adventure</p>

        <form className="auth-form" onSubmit={handleSubmit} noValidate>
          <div className="input-group">
            <label>Email</label>
            <div style={{ position: 'relative' }}>
              <Mail size={16} style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
              <input
                className={`input ${touched.email && errors.email ? 'input-error' : ''} ${touched.email && !errors.email && email ? 'input-success' : ''}`}
                type="email"
                placeholder="you@example.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                onBlur={() => handleBlur('email')}
                style={{ paddingLeft: '40px', width: '100%' }}
                id="login-email"
              />
            </div>
            {touched.email && errors.email && (
              <p className="validation-error"><AlertCircle size={13} /> {errors.email}</p>
            )}
          </div>

          <div className="input-group">
            <label>Password</label>
            <div style={{ position: 'relative' }}>
              <Lock size={16} style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
              <input
                className={`input ${touched.password && errors.password ? 'input-error' : ''} ${touched.password && !errors.password && password ? 'input-success' : ''}`}
                type="password"
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                onBlur={() => handleBlur('password')}
                style={{ paddingLeft: '40px', width: '100%' }}
                id="login-password"
              />
            </div>
            {touched.password && errors.password && (
              <p className="validation-error"><AlertCircle size={13} /> {errors.password}</p>
            )}
          </div>

          <button className="btn btn-primary btn-lg btn-full" type="submit" disabled={isLoading} id="login-submit">
            {isLoading ? 'Signing in...' : 'Sign In'}
          </button>
        </form>

        <p className="auth-footer">
          Don't have an account? <Link to="/register">Create one</Link>
        </p>
      </div>
    </div>
  );
}
