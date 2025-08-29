type KPIProps = { label: string; value: string | number; color?: string };

function KPI({ label, value, color = "primary" }: KPIProps) {
  return (
    <div className="card shadow-sm h-100">
      <div className="card-body text-center">
        <div className="text-muted small">{label}</div>
        <div className={`display-6 text-${color}`}>{value}</div>
      </div>
    </div>
  );
}
export default KPI;