// Contratos (DTOs) que viajan entre el frontend y la API. Reflejan los records del backend.

export interface LoginRequest { nombreUsuario: string; contrasena: string; }
export interface LoginResponse {
  token: string;
  nombreUsuario: string;
  clienteId: number | null;
  permisos: string[];
}

/** Respuesta paginada (solo lo que usamos: datos, página actual, total de páginas y tamaño). */
export interface Page<T> {
  content: T[];
  number: number;      // página actual (0-based)
  totalPages: number;  // cuántas páginas hay
  size: number;        // tamaño de página (cuántos por página)
}

export interface Cliente {
  id: number;
  nombre: string;
  apellido: string;
  numeroIdentificacion: string;
  fechaNacimiento: string;
  direccion?: string;
  correoElectronico?: string;
  telefono?: string;
}
export interface ClienteRequest {
  nombre: string;
  apellido: string;
  numeroIdentificacion: string;
  fechaNacimiento: string;
  direccion?: string;
  correoElectronico?: string;
  telefono?: string;
}

/** Autoservicio del cliente: solo datos de contacto. */
export interface ContactoRequest {
  direccion?: string;
  correoElectronico?: string;
  telefono?: string;
}

export interface Solicitud {
  id: number;
  clienteId: number;
  cliente: string;
  montoSolicitado: number;
  plazoMeses: number;
  proposito?: string;
  detalles?: string;
  estado: string;
  motivo?: string;
  fechaSolicitud: string;
}
export interface SolicitudRequest {
  clienteId: number;
  montoSolicitado: number;
  plazoMeses: number;
  proposito?: string;
  detalles?: string;
}

export interface Prestamo {
  id: number;
  solicitudId: number;
  clienteId: number;
  cliente: string;
  montoAprobado: number;
  plazoMeses: number;
  saldoPendiente: number;
  estadoPago: string;
  fechaAprobacion: string;
  proposito?: string;
  detalles?: string;
  motivoAprobacion?: string;
}

export interface Saldo {
  prestamoId: number;
  montoAprobado: number;
  totalPagado: number;
  saldoCalculado: number;
  saldoMaterializado: number;
}

export interface Pago {
  id: number;
  prestamoId: number;
  monto: number;
  numeroRecibo?: string;
  fechaPago: string;
  saldoPendiente: number;
  estadoPago: string;
}
export interface PagoRequest { monto: number; numeroRecibo?: string; }

/** Item del historial de pagos de un préstamo. */
export interface PagoHistorial {
  id: number;
  monto: number;
  numeroRecibo?: string;
  fechaPago: string;
}
