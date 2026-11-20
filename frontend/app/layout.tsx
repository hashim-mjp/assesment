import type { Metadata } from "next";
import Link from "next/link";
import "./globals.css";

export const metadata: Metadata = {
  title: "Support Tickets",
  description: "Support Ticket Management System",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body>
        <header className="app-header">
          <nav>
            <strong>Support Tickets</strong>
            <Link href="/tickets">Tickets</Link>
            <Link href="/tickets/new">New ticket</Link>
          </nav>
        </header>
        <main>{children}</main>
      </body>
    </html>
  );
}
