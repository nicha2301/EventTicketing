type Props = {
  id?: string;
  title: string;
  subtitle?: string;
  action?: React.ReactNode;
};

export default function SectionHeading({ id, title, subtitle, action }: Props) {
  return (
    <div id={id} className="container-page mb-8 flex flex-col items-start justify-between gap-4 sm:mb-10 sm:flex-row sm:items-end">
      <div>
        <h2 className="text-2xl font-semibold tracking-tight sm:text-3xl">{title}</h2>
        {subtitle ? (
          <p className="mt-1 text-slate-600">{subtitle}</p>
        ) : null}
      </div>
      {action ? <div className="shrink-0">{action}</div> : null}
    </div>
  );
}



