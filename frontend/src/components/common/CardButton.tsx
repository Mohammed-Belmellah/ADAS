import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import type { ReactNode } from "react";   // 👈 type-only import

type CardButtonProps = {
  title: string;
  to: string;
  fetchCount?: () => Promise<number>;
  subtitle?: string | ReactNode;
};

export default function CardButton({ title, to, fetchCount, subtitle }: CardButtonProps) {
  const navigate = useNavigate();
  const [count, setCount] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let mounted = true;
    if (!fetchCount) return;
    (async () => {
      try {
        const c = await fetchCount();
        if (mounted) setCount(c);
      } catch (e: any) {
        if (mounted) setError(e?.message ?? "Failed to load");
      }
    })();
    return () => { mounted = false; };
  }, [fetchCount]);

  return (
    <button className="card shadow-sm w-100 text-start btn p-0" onClick={() => navigate(to)}>
      <div className="card-body">
        <div className="d-flex align-items-baseline justify-content-between">
          <h5 className="card-title mb-0">{title}</h5>
          {count !== null && <span className="badge text-bg-secondary">{count}</span>}
        </div>
        {subtitle && <div className="text-muted mt-1">{subtitle}</div>}
        {error && <div className="text-danger small mt-2">{error}</div>}
      </div>
    </button>
  );
}
