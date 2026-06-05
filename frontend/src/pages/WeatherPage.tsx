import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useWeatherStore } from '../store/weatherStore';
import toast from 'react-hot-toast';
import {
  ArrowLeft, CloudRain, Sun, Thermometer, Droplets, Wind, AlertTriangle,
  RefreshCw, MapPin, Calendar, Leaf, Umbrella, Flame, Snowflake, CloudLightning, Eye
} from 'lucide-react';

export default function WeatherPage() {
  const { id } = useParams<{ id: string }>();
  const tripId = parseInt(id!);
  const navigate = useNavigate();
  const { forecasts, alerts, seasonalAdvice, isLoading, generateForecast, fetchForecasts, fetchAlerts, fetchSeasonalAdvice } = useWeatherStore();
  const [activeTab, setActiveTab] = useState<'forecast' | 'alerts' | 'seasonal'>('forecast');
  const [selectedLocation, setSelectedLocation] = useState<string>('all');

  useEffect(() => {
    fetchForecasts(tripId);
    fetchAlerts(tripId);
    fetchSeasonalAdvice(tripId);
  }, [tripId]);

  const handleGenerate = async () => {
    try {
      await generateForecast(tripId);
      await fetchAlerts(tripId);
      await fetchSeasonalAdvice(tripId);
      toast.success('Weather forecasts generated!');
    } catch {
      toast.error('Failed to generate forecasts');
    }
  };

  const locations = [...new Set(forecasts.map(f => f.locationName))];

  const filteredForecasts = selectedLocation === 'all'
    ? forecasts
    : forecasts.filter(f => f.locationName === selectedLocation);

  const getConditionColor = (condition: string) => {
    switch (condition) {
      case 'SUNNY': return '#f59e0b';
      case 'HOT': return '#ef4444';
      case 'RAINY': case 'HEAVY_RAIN': return '#3b82f6';
      case 'THUNDERSTORM': return '#7c3aed';
      case 'SNOWY': case 'COLD': return '#06b6d4';
      case 'CLOUDY': case 'FOGGY': return '#6b7280';
      default: return '#8b5cf6';
    }
  };

  const getSeverityColor = (severity: string) => {
    switch (severity) {
      case 'HIGH': return '#ef4444';
      case 'MEDIUM': return '#f59e0b';
      default: return '#3b82f6';
    }
  };

  return (
    <div className="animate-in">
      <button className="btn btn-ghost" onClick={() => navigate(`/trips/${id}`)} style={{ marginBottom: 16 }}>
        <ArrowLeft size={18} /> Back to Trip
      </button>

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <h2 style={{ fontSize: 24, fontWeight: 700, display: 'flex', alignItems: 'center', gap: 10 }}>
          <Sun size={24} style={{ color: '#f59e0b' }} /> Weather Planner
        </h2>
        <button className="btn btn-primary" onClick={handleGenerate} disabled={isLoading}>
          <RefreshCw size={14} className={isLoading ? 'sync-spin' : ''} />
          {isLoading ? 'Generating...' : 'Generate Forecast'}
        </button>
      </div>

      {/* ── Tabs ──────────────────────────────────────────── */}
      <div className="tab-bar" style={{ display: 'flex', gap: 4, marginBottom: 20, background: 'var(--bg-card)', padding: 4, borderRadius: 12 }}>
        {[
          { key: 'forecast' as const, label: 'Daily Forecast', icon: <Sun size={14} /> },
          { key: 'alerts' as const, label: `Alerts (${alerts.length})`, icon: <AlertTriangle size={14} /> },
          { key: 'seasonal' as const, label: 'Seasonal Advice', icon: <Leaf size={14} /> },
        ].map(tab => (
          <button
            key={tab.key}
            className={`btn btn-sm ${activeTab === tab.key ? 'btn-primary' : 'btn-ghost'}`}
            onClick={() => setActiveTab(tab.key)}
            style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 6, borderRadius: 10 }}
          >
            {tab.icon} {tab.label}
          </button>
        ))}
      </div>

      {/* ── FORECAST TAB ──────────────────────────────────── */}
      {activeTab === 'forecast' && (
        <>
          {/* Location filter */}
          {locations.length > 1 && (
            <div style={{ display: 'flex', gap: 6, marginBottom: 16, flexWrap: 'wrap' }}>
              <button className={`btn btn-sm ${selectedLocation === 'all' ? 'btn-primary' : 'btn-secondary'}`}
                onClick={() => setSelectedLocation('all')}>All Locations</button>
              {locations.map(loc => (
                <button key={loc}
                  className={`btn btn-sm ${selectedLocation === loc ? 'btn-primary' : 'btn-secondary'}`}
                  onClick={() => setSelectedLocation(loc)}>
                  <MapPin size={12} /> {loc}
                </button>
              ))}
            </div>
          )}

          {filteredForecasts.length === 0 ? (
            <div className="empty-state">
              <CloudRain size={48} style={{ color: 'var(--text-muted)', marginBottom: 12 }} />
              <h3>No forecasts yet</h3>
              <p>Click "Generate Forecast" to get weather data for your trip destinations</p>
            </div>
          ) : (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: 12 }}>
              {filteredForecasts.map(f => (
                <div key={f.id} className="card weather-card" style={{
                  borderLeft: `4px solid ${getConditionColor(f.condition)}`,
                  position: 'relative', overflow: 'hidden'
                }}>
                  {f.isAlert && (
                    <div className="weather-alert-badge">
                      <AlertTriangle size={10} /> Alert
                    </div>
                  )}
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
                    <span style={{ fontSize: 28 }}>{f.conditionIcon}</span>
                    <div style={{ textAlign: 'right' }}>
                      <div style={{ fontSize: 22, fontWeight: 700 }}>{Math.round(f.temperatureHigh)}°</div>
                      <div style={{ fontSize: 12, color: 'var(--text-muted)' }}>{Math.round(f.temperatureLow)}°</div>
                    </div>
                  </div>
                  <div style={{ fontSize: 13, fontWeight: 600, marginBottom: 2 }}>
                    {new Date(f.forecastDate).toLocaleDateString('en-IN', { weekday: 'short', month: 'short', day: 'numeric' })}
                  </div>
                  <div style={{ fontSize: 11, color: 'var(--text-muted)', marginBottom: 8, display: 'flex', alignItems: 'center', gap: 4 }}>
                    <MapPin size={10} /> {f.locationName}
                  </div>
                  <div style={{ fontSize: 12, marginBottom: 8 }}>{f.description}</div>

                  <div style={{ display: 'flex', gap: 12, fontSize: 11, color: 'var(--text-muted)', marginBottom: 8 }}>
                    <span style={{ display: 'flex', alignItems: 'center', gap: 3 }}><Droplets size={11} /> {f.humidityPercent}%</span>
                    <span style={{ display: 'flex', alignItems: 'center', gap: 3 }}><Umbrella size={11} /> {f.precipitationChance}%</span>
                    <span style={{ display: 'flex', alignItems: 'center', gap: 3 }}><Wind size={11} /> {f.windSpeedKmh} km/h</span>
                  </div>

                  <div style={{ fontSize: 11, padding: '6px 10px', borderRadius: 8, background: 'var(--bg-input)', color: 'var(--text-secondary)' }}>
                    💡 {f.recommendation}
                  </div>
                </div>
              ))}
            </div>
          )}
        </>
      )}

      {/* ── ALERTS TAB ────────────────────────────────────── */}
      {activeTab === 'alerts' && (
        <>
          {alerts.length === 0 ? (
            <div className="empty-state">
              <Sun size={48} style={{ color: '#22c55e', marginBottom: 12 }} />
              <h3>No weather alerts!</h3>
              <p>All clear — your trip has no extreme weather warnings</p>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
              {alerts.map((alert, i) => (
                <div key={i} className="card" style={{
                  borderLeft: `4px solid ${getSeverityColor(alert.severity)}`,
                  padding: '16px 20px'
                }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                      {alert.severity === 'HIGH' ? <CloudLightning size={18} style={{ color: '#ef4444' }} /> :
                        alert.severity === 'MEDIUM' ? <Flame size={18} style={{ color: '#f59e0b' }} /> :
                        <CloudRain size={18} style={{ color: '#3b82f6' }} />}
                      <span style={{ fontWeight: 600, fontSize: 14 }}>{alert.locationName}</span>
                    </div>
                    <span className="weather-severity-badge" style={{
                      background: `${getSeverityColor(alert.severity)}15`,
                      color: getSeverityColor(alert.severity)
                    }}>
                      {alert.severity}
                    </span>
                  </div>
                  <div style={{ fontSize: 12, color: 'var(--text-muted)', marginBottom: 4, display: 'flex', alignItems: 'center', gap: 4 }}>
                    <Calendar size={11} /> {alert.forecastDate} · {alert.condition.replace(/_/g, ' ')}
                  </div>
                  <div style={{ fontSize: 13, marginBottom: 10 }}>{alert.alertMessage}</div>
                  <div style={{
                    fontSize: 12, padding: '8px 12px', borderRadius: 8,
                    background: 'rgba(99, 102, 241, 0.06)', border: '1px solid rgba(99, 102, 241, 0.15)'
                  }}>
                    <span style={{ fontWeight: 600, color: '#6366f1' }}>🔄 Recommended Swap: </span>
                    {alert.suggestedSwap}
                  </div>
                </div>
              ))}
            </div>
          )}
        </>
      )}

      {/* ── SEASONAL TAB ──────────────────────────────────── */}
      {activeTab === 'seasonal' && (
        <>
          {seasonalAdvice.length === 0 ? (
            <div className="empty-state">
              <Leaf size={48} style={{ color: 'var(--text-muted)', marginBottom: 12 }} />
              <h3>No seasonal data</h3>
              <p>Generate weather forecasts first to get seasonal insights</p>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
              {seasonalAdvice.map((sa, i) => (
                <div key={i} className="card" style={{ padding: '20px 24px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 12 }}>
                    <MapPin size={18} style={{ color: 'var(--accent-primary)' }} />
                    <span style={{ fontSize: 17, fontWeight: 700 }}>{sa.locationName}</span>
                    <span className="badge badge-indigo" style={{ fontSize: 10 }}>{sa.season}</span>
                  </div>
                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12, marginBottom: 14 }}>
                    <div className="weather-info-chip">
                      <Thermometer size={14} style={{ color: '#ef4444' }} />
                      <div>
                        <div style={{ fontSize: 11, color: 'var(--text-muted)' }}>Temperature</div>
                        <div style={{ fontSize: 13, fontWeight: 600 }}>{sa.temperatureRange}</div>
                      </div>
                    </div>
                    <div className="weather-info-chip">
                      <Eye size={14} style={{ color: '#22c55e' }} />
                      <div>
                        <div style={{ fontSize: 11, color: 'var(--text-muted)' }}>Best Time</div>
                        <div style={{ fontSize: 13, fontWeight: 600 }}>{sa.bestTimeToVisit}</div>
                      </div>
                    </div>
                  </div>
                  <div style={{ fontSize: 13, marginBottom: 14, lineHeight: 1.5 }}>{sa.advice}</div>
                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
                    <div>
                      <div style={{ fontSize: 12, fontWeight: 600, marginBottom: 6, color: '#22c55e' }}>🎯 Recommended Activities</div>
                      {sa.recommendedActivities.map((a, j) => (
                        <div key={j} style={{ fontSize: 12, padding: '4px 0', color: 'var(--text-secondary)' }}>• {a}</div>
                      ))}
                    </div>
                    <div>
                      <div style={{ fontSize: 12, fontWeight: 600, marginBottom: 6, color: '#6366f1' }}>🎒 Items to Pack</div>
                      {sa.itemsToPack.map((item, j) => (
                        <div key={j} style={{ fontSize: 12, padding: '4px 0', color: 'var(--text-secondary)' }}>• {item}</div>
                      ))}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </>
      )}
    </div>
  );
}
