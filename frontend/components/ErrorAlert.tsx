import type { ApiRequestError } from "@/lib/api";

type Props = {
  error: ApiRequestError | string | null;
  className?: string;
};

/**
 * Shows API `message` (or a plain string). Field-level hints belong under controls.
 */
export function ErrorAlert({ error, className }: Props) {
  if (!error) return null;

  const message =
    typeof error === "string" ? error : error.message || "Something went wrong";

  return (
    <div
      className={`alert alert-error${className ? ` ${className}` : ""}`}
      role="alert"
    >
      {message}
    </div>
  );
}
