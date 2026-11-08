/**
 * Standard Spring Boot error response matching ApiErrorResponse.java
 */
export interface ApiErrorResponse {
  status: number;
  error: string;
  message: string;
  timestamp: string;
}

/**
 * Spring Data PagedModel page metadata
 */
export interface PageMetadata {
  size: number;
  totalElements: number;
  totalPages: number;
  number: number;
}

/**
 * Spring Data PagedModel response wrapper
 */
export interface PagedModel<T> {
  content: T[];
  page: PageMetadata;
}
