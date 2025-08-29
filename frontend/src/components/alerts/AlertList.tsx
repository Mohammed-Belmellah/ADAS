const dummyAlerts = [
  { id: "1", type: "EXTREME_FATIGUE", message: "Driver John is tired!", createdAt: "10:00" },
  { id: "2", type: "FREQUENT_PEAKS",  message: "Driver Sarah shows anger peaks.", createdAt: "10:05" },
];

function AlertList() {
  return (
    <div className="card">
      <div className="card-header">Recent Alerts</div>
      <ul className="list-group list-group-flush">
        {dummyAlerts.map(a => (
          <li key={a.id} className="list-group-item d-flex justify-content-between">
            <div><strong>{a.type}</strong> — {a.message}</div>
            <span className="text-muted">{a.createdAt}</span>
          </li>
        ))}
      </ul>
    </div>
  );
}
export default AlertList;
