// Tìm kiếm bằng input
document.getElementById("datatables-search").addEventListener("input", (e) => {
  const instance = mdb.Datatable.getInstance(
    document.getElementById("datatables-table")
  );
  instance.search(e.target.value); // Tìm kiếm mặc định trên tất cả cột
});

// Tìm kiếm nâng cao (nếu có)
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

// Lọc theo Role
document.getElementById("role-filter").addEventListener("change", (e) => {
  const statusFilter = e.target.value;
  console.log("Filter selected:", statusFilter);

  const instance = mdb.Datatable.getInstance(
    document.getElementById("datatables-table")
  );
  instance.search((row) => {
    const productStatus = row[2] || ""; // Cột Role
    console.log(
      "Row roles:",
      productStatus,
      "Matches?",
      productStatus.includes(statusFilter)
    );
    return statusFilter === "" || productStatus.includes(statusFilter);
  });
});

// Lọc theo Status (nếu có cột Status ở vị trí thứ 6)
document.getElementById("status-filter").addEventListener("change", (e) => {
  const statusFilter = e.target.value;
  const instance = mdb.Datatable.getInstance(
    document.getElementById("datatables-table")
  );
  instance.search((row) => {
    const productStatus = row[5] || ""; // Cột Status (chỉ số 5 vì thứ 6 trong bảng)
    return statusFilter === "" || productStatus === statusFilter; // So sánh chính xác
  });
});
