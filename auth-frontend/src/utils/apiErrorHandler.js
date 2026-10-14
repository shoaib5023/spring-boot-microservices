
export function getApiErrorMessage(error, serviceName) {
  // No HTTP response: server may be down, unreachable, or blocked by CORS.
  if (!error.response) {
    return {
      status: 503,
      message: `${serviceName} is currently unavailable. Please try again later.`,
    };
  }

  const status = error.response.status;

  if (status === 503) {
    return {
      status,
      message: `${serviceName} is temporarily unavailable. Please try again later.`,
    };
  }

  if (status === 504) {
    return {
      status,
      message: "The request timed out. Please try again.",
    };
  }

  if (status === 401) {
    return {
      status,
      message: "Your session has expired. Please log in again.",
    };
  }

  return {
    status,
    message:
      error.response.data?.message ||
      `Request failed with status ${status}.`,
  };
}
