// ==========================================================
// Modelos (interfaces) que reflejan los JSON de la API Spring Boot
// ==========================================================

export type NombreRol = 'ADMIN' | 'USER' | 'GUEST';
export type EstadoCampana = 'PLANIFICADA' | 'EN_CURSO' | 'FINALIZADA';

export const ESTADOS_CAMPANA: EstadoCampana[] = ['PLANIFICADA', 'EN_CURSO', 'FINALIZADA'];
export const ROLES: NombreRol[] = ['ADMIN', 'USER', 'GUEST'];

// ---------- Autenticación ----------
export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  email: string;
  nombreCompleto: string;
}

/** Respuesta de POST /api/auth/login y /api/auth/register */
export interface AuthResponse {
  token: string;
  tipo: string;
  username: string;
  roles: string[]; // p. ej. ["ROLE_ADMIN"]
  expiraEnMs: number;
}

/** Sesión guardada en el navegador (token JWT + datos mínimos del usuario) */
export interface Sesion {
  token: string;
  username: string;
  roles: NombreRol[];
  expiraEn: number; // marca de tiempo (ms) en la que vence el token
}

// ---------- Entidades de negocio ----------
export interface Productor {
  id: number;
  nombres: string;
  apellidos: string;
  dni: string;
  telefono?: string | null;
  email?: string | null;
}

export type ProductorRequest = Omit<Productor, 'id'>;

export interface Parcela {
  id: number;
  nombre: string;
  ubicacion?: string | null;
  areaHectareas: number;
  productor: Productor;
}

export interface ParcelaRequest {
  nombre: string;
  ubicacion: string | null;
  areaHectareas: number;
  productorId: number;
}

export interface Cultivo {
  id: number;
  nombre: string;
  variedad?: string | null;
  cicloDiasEstimado?: number | null;
}

export type CultivoRequest = Omit<Cultivo, 'id'>;

export interface Campana {
  id: number;
  fechaInicio: string; // ISO yyyy-MM-dd
  fechaFinEstimada?: string | null;
  estado: EstadoCampana;
  cultivo: Cultivo;
  parcela: Parcela;
}

export interface CampanaRequest {
  fechaInicio: string;
  fechaFinEstimada: string | null;
  estado: EstadoCampana;
  cultivo: { id: number };
  parcela: { id: number };
}

export interface Cosecha {
  id: number;
  fecha: string;
  cantidadKg: number;
  observacion?: string | null;
  campana: Campana;
}

export interface CosechaRequest {
  fecha: string;
  cantidadKg: number;
  observacion: string | null;
  campanaId?: number;
}

export interface Usuario {
  id: number;
  username: string;
  email: string;
  nombreCompleto: string;
  enabled: boolean;
  roles: string[];
}

// ---------- Errores de la API (GlobalExceptionHandler) ----------
export interface ApiError {
  timestamp?: string;
  status: number;
  error: string;
  detalles?: Record<string, string>;
}
