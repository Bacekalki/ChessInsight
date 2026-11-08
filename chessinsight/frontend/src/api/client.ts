import { ApiErrorResponse } from '../types/api.types';

export class ApiClientError extends Error {
  public readonly status: number;
  public readonly errorTitle: string;
  public readonly timestamp?: string;

  constructor(status: number, errorTitle: string, message: string, timestamp?: string) {
    super(message);
    this.name = 'ApiClientError';
    this.status = status;
    this.errorTitle = errorTitle;
    this.timestamp = timestamp;
  }
}

/**
 * Generic fetch wrapper with unified JSON deserialization and Spring Boot error handling.
 */
export async function apiClient<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
  const headers = new Headers(options.headers || {});

  // If body is not FormData, default to application/json
  if (!(options.body instanceof FormData) && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }

  const config: RequestInit = {
    ...options,
    headers,
  };

  const response = await fetch(endpoint, config);

  if (!response.ok) {
    let errorResponse: ApiErrorResponse | null = null;
    try {
      errorResponse = await response.json();
    } catch {
      // Body is not JSON
    }

    if (errorResponse && errorResponse.message) {
      throw new ApiClientError(
        errorResponse.status || response.status,
        errorResponse.error || response.statusText,
        errorResponse.message,
        errorResponse.timestamp
      );
    }

    throw new ApiClientError(
      response.status,
      response.statusText,
      `Request to ${endpoint} failed with status ${response.status}`
    );
  }

  // Handle empty responses (204 No Content, etc.)
  if (response.status === 204) {
    return {} as T;
  }

  const contentType = response.headers.get('content-type');
  if (contentType && contentType.includes('application/json')) {
    return (await response.json()) as T;
  }

  return (await response.text()) as unknown as T;
}

/**
 * Helper to download binary or text streams (e.g. PGN files)
 */
export async function downloadFile(endpoint: string, fallbackFilename: string): Promise<void> {
  const response = await fetch(endpoint);

  if (!response.ok) {
    let message = `Failed to download file (status ${response.status})`;
    try {
      const err = await response.json();
      if (err.message) message = err.message;
    } catch {
      // not json
    }
    throw new ApiClientError(response.status, response.statusText, message);
  }

  const blob = await response.blob();
  const contentDisposition = response.headers.get('content-disposition');
  let filename = fallbackFilename;

  if (contentDisposition) {
    const filenameMatch = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/);
    if (filenameMatch && filenameMatch[1]) {
      filename = filenameMatch[1].replace(/['"]/g, '');
    }
  }

  const blobUrl = window.URL.createObjectURL(blob);
  const anchor = document.createElement('a');
  anchor.href = blobUrl;
  anchor.download = filename;
  document.body.appendChild(anchor);
  anchor.click();
  document.body.removeChild(anchor);
  window.URL.revokeObjectURL(blobUrl);
}
