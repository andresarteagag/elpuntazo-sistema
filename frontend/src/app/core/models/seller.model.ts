export interface Seller {
  id: number;
  name: string;
  email: string;
  active: boolean;
}

export interface CreateSellerRequest {
  name: string;
  email: string;
  password: string;
}

export interface UpdateSellerRequest {
  name: string;
  newPassword?: string;
}
