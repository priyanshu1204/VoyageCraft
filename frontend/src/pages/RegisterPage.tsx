import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import toast from 'react-hot-toast';
import { Compass, Mail, Lock, User, AlertCircle, CheckCircle2 } from 'lucide-react';

// Validation patterns
const NAME_REGEX = /^[A-Za-z]{2,30}$/;
const EMAIL_REGEX = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
const PASSWORD_MIN_LENGTH = 8;
const PASSWORD_UPPERCASE = /[A-Z]/;
const PASSWORD_LOWERCASE = /[a-z]/;
const PASSWORD_DIGIT = /[0-9]/;
const PASSWORD_SPECIAL = /[!@#$%^&*(),.?":{}|<>]/;

interface ValidationError {
  firstName?: string;
  lastName?: string;
  email?: string;
  password?: string[];
}

export default function RegisterPage() {
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [errors, setErrors] = useState<ValidationError>({});
  const [touched, setTouched] = useState<Record<string, boolean>>({});
  const { register, isLoading } = useAuthStore();
  const navigate = useNavigate();

  const validateFirstName = (value: string): string | undefined => {
    if (!value.trim()) return 'First name is required.';
    if (value.trim().length < 2) return 'First name must be at least 2 characters.';
    if (!NAME_REGEX.test(value.trim())) return 'First name should contain only letters (A-Z). No numbers or special characters.';
    return undefined;
  };

  const validateLastName = (value: string): string | undefined => {
    if (!value.trim()) return 'Last name is required.';
    if (value.trim().length < 2) return 'Last name must be at least 2 characters.';
    if (!NAME_REGEX.test(value.trim())) return 'Last name should contain only letters (A-Z). No numbers or special characters.';
    return undefined;
  };

  const validateEmail = (value: string): string | undefined => {
    if (!value.trim()) return 'Email address is required.';
    if (!EMAIL_REGEX.test(value.trim())) return 'Please enter a valid email (e.g., john@example.com).';
    return undefined;
  };

  const validatePassword = (value: string): string[] => {
    const issues: string[] = [];
    if (value.length < PASSWORD_MIN_LENGTH) issues.push(`At least ${PASSWORD_MIN_LENGTH} characters`);
    if (!PASSWORD_UPPERCASE.test(value)) issues.push('At least one uppercase letter (A-Z)');
    if (!PASSWORD_LOWERCASE.test(value)) issues.push('At least one lowercase letter (a-z)');
    if (!PASSWORD_DIGIT.test(value)) issues.push('At least one number (0-9)');
    if (!PASSWORD_SPECIAL.test(value)) issues.push('At least one special character (!@#$%^&*)');
    return issues;
  };

  const validateAll = (): boolean => {
    const newErrors: ValidationError = {};
    newErrors.firstName = validateFirstName(firstName);
    newErrors.lastName = validateLastName(lastName);
    newErrors.email = validateEmail(email);
    const pwdIssues = validatePassword(password);
    if (pwdIssues.length > 0) newErrors.password = pwdIssues;
    setErrors(newErrors);
    return !newErrors.firstName && !newErrors.lastName && !newErrors.email && (!newErrors.password || newErrors.password.length === 0);
  };

  const handleBlur = (field: string) => {
    setTouched(prev => ({ ...prev, [field]: true }));
    // Validate specific field on blur
    const newErrors = { ...errors };
    if (field === 'firstName') newErrors.firstName = validateFirstName(firstName);
    if (field === 'lastName') newErrors.lastName = validateLastName(lastName);
    if (field === 'email') newErrors.email = validateEmail(email);
    if (field === 'password') {
      const pwdIssues = validatePassword(password);
      newErrors.password = pwdIssues.length > 0 ? pwdIssues : undefined;
    }
    setErrors(newErrors);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    // Mark all as touched
    setTouched({ firstName: true, lastName: true, email: true, password: true });

    if (!validateAll()) {
      toast.error('Please fix the errors below before submitting.');
      return;
    }

    try {
      await register({ firstName: firstName.trim(), lastName: lastName.trim(), email: email.trim(), password });
      toast.success('Account created! Welcome aboard!');
      navigate('/dashboard');
    } catch (err: any) {
      toast.error(err.message || 'Registration failed. Please try again.');
    }
  };

  const passwordIssues = validatePassword(password);
  const passwordValid = password.length > 0 && passwordIssues.length === 0;

  return (
    <div className="auth-container">
      <div className="auth-card animate-in">
        <div style={{ display: 'flex', justifyContent: 'center', marginBottom: '16px' }}>
          <Compass size={48} style={{ color: '#6366f1' }} />
        </div>
        <h1>Join VoyageCraft</h1>
        <p className="auth-subtitle">Start planning your dream trips today</p>

        <form className="auth-form" onSubmit={handleSubmit} noValidate>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
            {/* First Name */}
            <div className="input-group">
              <label>First Name</label>
              <div style={{ position: 'relative' }}>
                <User size={16} style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
                <input
                  className={`input ${touched.firstName && errors.firstName ? 'input-error' : ''} ${touched.firstName && !errors.firstName && firstName ? 'input-success' : ''}`}
                  placeholder="John"
                  value={firstName}
                  onChange={(e) => setFirstName(e.target.value)}
                  onBlur={() => handleBlur('firstName')}
                  style={{ paddingLeft: '40px', width: '100%' }}
                  id="register-firstname"
                />
              </div>
              {touched.firstName && errors.firstName && (
                <p className="validation-error"><AlertCircle size={13} /> {errors.firstName}</p>
              )}
            </div>

            {/* Last Name */}
            <div className="input-group">
              <label>Last Name</label>
              <input
                className={`input ${touched.lastName && errors.lastName ? 'input-error' : ''} ${touched.lastName && !errors.lastName && lastName ? 'input-success' : ''}`}
                placeholder="Doe"
                value={lastName}
                onChange={(e) => setLastName(e.target.value)}
                onBlur={() => handleBlur('lastName')}
                style={{ width: '100%' }}
                id="register-lastname"
              />
              {touched.lastName && errors.lastName && (
                <p className="validation-error"><AlertCircle size={13} /> {errors.lastName}</p>
              )}
            </div>
          </div>

          {/* Email */}
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
                id="register-email"
              />
            </div>
            {touched.email && errors.email && (
              <p className="validation-error"><AlertCircle size={13} /> {errors.email}</p>
            )}
          </div>

          {/* Password */}
          <div className="input-group">
            <label>Password</label>
            <div style={{ position: 'relative' }}>
              <Lock size={16} style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
              <input
                className={`input ${touched.password && !passwordValid ? 'input-error' : ''} ${passwordValid ? 'input-success' : ''}`}
                type="password"
                placeholder="Create a strong password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                onBlur={() => handleBlur('password')}
                style={{ paddingLeft: '40px', width: '100%' }}
                id="register-password"
              />
            </div>

            {/* Password strength checklist */}
            {(touched.password || password.length > 0) && (
              <div className="password-checklist">
                <p style={{ fontSize: '12px', fontWeight: 600, marginBottom: '6px', color: 'var(--text-secondary)' }}>
                  Password must contain:
                </p>
                {[
                  { label: `At least ${PASSWORD_MIN_LENGTH} characters`, valid: password.length >= PASSWORD_MIN_LENGTH },
                  { label: 'One uppercase letter (A-Z)', valid: PASSWORD_UPPERCASE.test(password) },
                  { label: 'One lowercase letter (a-z)', valid: PASSWORD_LOWERCASE.test(password) },
                  { label: 'One number (0-9)', valid: PASSWORD_DIGIT.test(password) },
                  { label: 'One special character (!@#$%^&*)', valid: PASSWORD_SPECIAL.test(password) },
                ].map((rule, i) => (
                  <div key={i} className={`password-rule ${rule.valid ? 'rule-pass' : 'rule-fail'}`}>
                    {rule.valid ? <CheckCircle2 size={13} /> : <AlertCircle size={13} />}
                    <span>{rule.label}</span>
                  </div>
                ))}
              </div>
            )}
          </div>

          <button className="btn btn-primary btn-lg btn-full" type="submit" disabled={isLoading} id="register-submit">
            {isLoading ? 'Creating account...' : 'Create Account'}
          </button>
        </form>

        <p className="auth-footer">
          Already have an account? <Link to="/login">Sign in</Link>
        </p>
      </div>
    </div>
  );
}
