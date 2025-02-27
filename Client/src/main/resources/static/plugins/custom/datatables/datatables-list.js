document.getElementById("datatables-search").addEventListener("input", (e) => {
  const instance = mdb.Datatable.getInstance(
    document.getElementById("datatables-table")
  );

  instance.search(e.target.value);
});

const advancedSearchInput = document.getElementById("advanced-search-input");

const search = (value) => {
  const instance = mdb.Datatable.getInstance(
    document.getElementById("datatable-advanced-search")
  );

  let [phrase, columns] = value.split(" in:").map((str) => str.trim());

  if (columns) {
    columns = columns.split(",").map((str) => str.toLowerCase().trim());
  }

  instance.search(phrase, columns);
};

document.getElementById("status-filter").addEventListener("change", (e) => {
  const statusFilter = e.target.value;
  const rows = document.querySelectorAll("#datatables-table tbody tr");

  rows.forEach((row) => {
    const statusCell = row.querySelector("td:nth-child(6)");
    const productStatus = statusCell ? statusCell.textContent.trim() : "";

    if (statusFilter === "" || productStatus === statusFilter) {
      row.style.display = "";
    } else {
      row.style.display = "none";
    }
  });
});
