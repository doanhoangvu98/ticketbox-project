# TicketBox Web

React 19 · Vite · TypeScript · Tailwind v4 · TanStack Query · axios · MSW · react-toastify · VietQR · Remix Icon.

```bash
pnpm install
pnpm dev         # http://localhost:8000
```

- `.env.local`: `VITE_USE_MOCK=true` runs entirely on mock data (no backend needed).
  `VITE_MOCK_EXCLUDE=auth,events` sends those groups to the real backend instead.
- Demo accounts (password `123456`): `customer@`, `organizer@`, `staff@`, `admin@ticketbox.vn`,
  or use the **MOCK** button in the bottom-left corner.
