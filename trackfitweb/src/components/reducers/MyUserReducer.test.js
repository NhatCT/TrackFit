import MyUserReducer from './MyUserReducer';

// Mock react-cookies
jest.mock('react-cookies', () => ({
  save: jest.fn(),
  remove: jest.fn(),
}));

const cookie = require('react-cookies');

describe('MyUserReducer', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('login action', () => {
    test('sets user from payload', () => {
      const user = { username: 'test', role: 'USER' };
      const result = MyUserReducer(null, { type: 'login', payload: user });
      expect(result).toEqual(user);
    });

    test('saves user to cookie', () => {
      const user = { username: 'test' };
      MyUserReducer(null, { type: 'login', payload: user });
      expect(cookie.save).toHaveBeenCalledWith('user', user, { path: '/' });
    });

    test('null payload returns null', () => {
      const result = MyUserReducer({ username: 'old' }, { type: 'login', payload: null });
      expect(result).toBe(null);
    });
  });

  describe('logout action', () => {
    test('returns null', () => {
      const result = MyUserReducer({ username: 'test' }, { type: 'logout' });
      expect(result).toBe(null);
    });

    test('removes user and token cookies', () => {
      MyUserReducer({ username: 'test' }, { type: 'logout' });
      expect(cookie.remove).toHaveBeenCalledWith('user', { path: '/' });
      expect(cookie.remove).toHaveBeenCalledWith('token', { path: '/' });
    });
  });

  describe('updateAvatar action', () => {
    test('updates avatar URL when logged in', () => {
      const current = { username: 'test', avatarUrl: 'old.jpg' };
      const result = MyUserReducer(current, { type: 'updateAvatar', payload: 'new.jpg' });
      expect(result.avatarUrl).toBe('new.jpg');
      expect(result.avatarVersion).toBeDefined();
      expect(result.username).toBe('test');
    });

    test('returns current state when not logged in', () => {
      const result = MyUserReducer(null, { type: 'updateAvatar', payload: 'new.jpg' });
      expect(result).toBe(null);
    });

    test('saves updated user to cookie', () => {
      const current = { username: 'test' };
      MyUserReducer(current, { type: 'updateAvatar', payload: 'new.jpg' });
      expect(cookie.save).toHaveBeenCalled();
    });
  });

  describe('updateProfile action', () => {
    test('merges profile fields', () => {
      const current = { username: 'test', firstName: 'Old', lastName: 'Name' };
      const result = MyUserReducer(current, {
        type: 'updateProfile',
        payload: { firstName: 'New', email: 'new@test.com' }
      });
      expect(result.firstName).toBe('New');
      expect(result.lastName).toBe('Name');
      expect(result.email).toBe('new@test.com');
      expect(result.username).toBe('test');
    });

    test('returns current state when not logged in', () => {
      const result = MyUserReducer(null, {
        type: 'updateProfile',
        payload: { firstName: 'New' }
      });
      expect(result).toBe(null);
    });

    test('saves updated profile to cookie', () => {
      const current = { username: 'test' };
      MyUserReducer(current, { type: 'updateProfile', payload: { firstName: 'X' } });
      expect(cookie.save).toHaveBeenCalled();
    });
  });

  describe('default action', () => {
    test('returns current state for unknown action', () => {
      const current = { username: 'test' };
      const result = MyUserReducer(current, { type: 'unknown' });
      expect(result).toBe(current);
    });
  });
});
