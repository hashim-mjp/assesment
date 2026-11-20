import { redirect } from "next/navigation";

/** App entry redirects to the ticket list route from ui-flow.md. */
export default function Home() {
  redirect("/tickets");
}
