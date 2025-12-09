You are redesigning the restaurant frontends in this repo.

Inputs:

- Current restaurant site (for content and tone): <link>
- Brand style text (colors, typography vibe, voice, imagery cues): <brand-style-text>
- Stripe/payment info and any other providers to enable: <payment-details>
- Logo/asset URLs (if any): <assets>
- Menu/product data source (or confirm to keep existing sample data): <data-source>

Requirements:

- Build a unique, on-brand customer website using the `website` app; keep existing backend endpoints and flows intact.
- Keep KDS simple: no bump/recall, no per-station routing. Ensure availability toggles in KDS instantly reflect on the customer website (86 products/categories).
- Support multi-provider payments (Stripe at minimum) using the payment abstraction; wire provider choice via config/env.
- Ensure receipts support PDF and ESC/POS output; keep config-driven printer/device settings.
- Single-restaurant deployment (not multi-tenant).
- Update copy, colors, and layout per the brand style text; replace stock imagery with brand-appropriate placeholders/URLs.
- Preserve core flows: browse, add to cart, checkout with online payment, order tracking, auth, product/category management, statistics, receipts.
- Keep code changes scoped to frontend theming/content and necessary config for payments/assets; do not break APIs.

Deliverables:

- Updated `website` UI with new styling, imagery, and copy.
- Any config changes for payment provider selection and assets.
- Brief notes on what changed and how to run/build.
