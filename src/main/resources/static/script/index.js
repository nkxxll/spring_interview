async function changeHeader() {
  try {
    const res = await fetch("/api/newheader");

    if (!res.ok) throw new Error(`Status: ${res.status}`);

    // This is the key change: use .text() instead of .body
    const content = await res.text();
    header.textContent = content;

  } catch (err) {
    console.error(err);
    header.textContent = "There was an error fetching the header";
  }
}

headerChange.onclick = changeHeader;
