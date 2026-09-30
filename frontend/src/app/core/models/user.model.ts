export type UserRole = 'ADMIN' | 'VENDEDOR';

export interface AuthenticatedUser {
  userId: number;
  name: string;
  email: string;
  role: UserRole;
}

export interface LoginResponse {
  token: string;
  userId: number;
  name: string;
  email: string;
  role: UserRole;
}
