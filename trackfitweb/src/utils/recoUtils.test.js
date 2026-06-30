import { toInt, formatScore, getKey, normalizeItem, buildQueryParams } from './recoUtils';

describe('toInt', () => {
  test('returns number for valid numeric string', () => {
    expect(toInt('42')).toBe(42);
  });

  test('returns fallback for empty string', () => {
    expect(toInt('', 10)).toBe(10);
  });

  test('returns fallback for null', () => {
    expect(toInt(null, 5)).toBe(5);
  });

  test('returns fallback for undefined', () => {
    expect(toInt(undefined, 0)).toBe(0);
  });

  test('returns fallback for non-numeric string', () => {
    expect(toInt('abc', 99)).toBe(99);
  });

  test('returns fallback for Infinity', () => {
    expect(toInt('Infinity', 0)).toBe(0);
  });

  test('handles zero correctly', () => {
    expect(toInt('0')).toBe(0);
  });

  test('handles float values', () => {
    expect(toInt('3.14')).toBe(3.14);
  });

  test('default fallback is null', () => {
    expect(toInt('')).toBe(null);
  });
});

describe('formatScore', () => {
  test('null returns null', () => {
    expect(formatScore(null)).toBe(null);
  });

  test('undefined returns null', () => {
    expect(formatScore(undefined)).toBe(null);
  });

  test('decimal 0..1 converts to percentage', () => {
    expect(formatScore(0.85)).toBe('85%');
  });

  test('value of 1 treated as 100%', () => {
    expect(formatScore(1)).toBe('100%');
  });

  test('value above 1 treated as already percentage', () => {
    expect(formatScore(75)).toBe('75%');
  });

  test('zero returns 0%', () => {
    expect(formatScore(0)).toBe('0%');
  });

  test('rounds decimal properly', () => {
    expect(formatScore(0.666)).toBe('67%');
  });
});

describe('getKey', () => {
  test('uses exerciseId if available', () => {
    expect(getKey({ exerciseId: 42 })).toBe('42');
  });

  test('falls back to idxKey', () => {
    expect(getKey({ idxKey: 'idx-5' })).toBe('idx-5');
  });

  test('falls back to name', () => {
    expect(getKey({ name: 'Bench Press' })).toBe('Bench Press');
  });

  test('returns unknown for empty object', () => {
    expect(getKey({})).toBe('unknown');
  });

  test('handles null/undefined', () => {
    expect(getKey(null)).toBe('unknown');
    expect(getKey(undefined)).toBe('unknown');
  });
});

describe('normalizeItem', () => {
  test('normalizes standard API response', () => {
    const item = {
      exerciseId: 1,
      name: 'Squat',
      estimatedMinutes: 15,
      description: 'Leg exercise',
      muscleGroup: 'Legs',
      difficulty: 'Medium',
      score: 0.9,
      reason: 'Good match'
    };
    const result = normalizeItem(item, 0);
    expect(result.exerciseId).toBe(1);
    expect(result.name).toBe('Squat');
    expect(result.estimatedMinutes).toBe(15);
    expect(result.description).toBe('Leg exercise');
    expect(result.muscleGroup).toBe('Legs');
    expect(result.difficulty).toBe('Medium');
    expect(result.score).toBe(0.9);
    expect(result.reason).toBe('Good match');
    expect(result.idxKey).toBe('idx-0');
  });

  test('normalizes alternative field names', () => {
    const item = {
      id: 5,
      exerciseName: 'Push Up',
      minutes: 10,
      desc: 'Upper body',
      type: 'Chest',
      intensity: 'High'
    };
    const result = normalizeItem(item, 3);
    expect(result.exerciseId).toBe(5);
    expect(result.name).toBe('Push Up');
    expect(result.estimatedMinutes).toBe(10);
    expect(result.description).toBe('Upper body');
    expect(result.muscleGroup).toBe('Chest');
    expect(result.difficulty).toBe('High');
  });

  test('handles missing fields with defaults', () => {
    const result = normalizeItem({}, 7);
    expect(result.exerciseId).toBe(null);
    expect(result.name).toBe('Bài tập');
    expect(result.estimatedMinutes).toBe(null);
    expect(result.description).toBe('');
    expect(result.muscleGroup).toBe('');
    expect(result.difficulty).toBe('');
    expect(result.score).toBe(null);
    expect(result.reason).toBe('');
    expect(result.idxKey).toBe('idx-7');
  });

  test('exercisesId field maps correctly', () => {
    const item = { exercisesId: 99 };
    const result = normalizeItem(item, 0);
    expect(result.exerciseId).toBe(99);
  });
});

describe('buildQueryParams', () => {
  test('default size is 8', () => {
    const result = buildQueryParams({});
    expect(result.size).toBe(8);
  });

  test('custom size', () => {
    const result = buildQueryParams({ size: 12 });
    expect(result.size).toBe(12);
  });

  test('includes availableMinutes when provided', () => {
    const result = buildQueryParams({ availableMinutes: 30 });
    expect(result.availableMinutes).toBe(30);
  });

  test('excludes null availableMinutes', () => {
    const result = buildQueryParams({ availableMinutes: null });
    expect(result.availableMinutes).toBeUndefined();
  });

  test('trims keyword', () => {
    const result = buildQueryParams({ kw: '  chest  ' });
    expect(result.kw).toBe('chest');
  });

  test('excludes empty keyword', () => {
    const result = buildQueryParams({ kw: '   ' });
    expect(result.kw).toBeUndefined();
  });

  test('includes intensity and goalType', () => {
    const result = buildQueryParams({ intensity: 'High', goalType: 'cardio' });
    expect(result.intensity).toBe('High');
    expect(result.goalType).toBe('cardio');
  });

  test('excludes empty intensity and goalType', () => {
    const result = buildQueryParams({ intensity: '', goalType: '' });
    expect(result.intensity).toBeUndefined();
    expect(result.goalType).toBeUndefined();
  });
});
