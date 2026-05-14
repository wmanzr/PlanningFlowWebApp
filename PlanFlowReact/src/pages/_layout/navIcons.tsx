import type { SVGAttributes } from 'react';
const Icon = (props: SVGAttributes<SVGSVGElement>) => (<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={1.75} strokeLinecap="round" strokeLinejoin="round" aria-hidden {...props}/>);
export const IconMenu = (props: SVGAttributes<SVGSVGElement>) => (<Icon {...props}>
    <path d="M4 6h16M4 12h16M4 18h16"/>
  </Icon>);
export const IconSun = (props: SVGAttributes<SVGSVGElement>) => (<Icon {...props}>
    <circle cx={12} cy={12} r={4}/>
    <path d="M12 2v2m0 16v2M4.93 4.93l1.41 1.41m11.32 11.32 1.41 1.41M2 12h2m16 0h2M4.93 19.07l1.41-1.41M17.66 6.34l1.41-1.41"/>
  </Icon>);
export const IconMoon = (props: SVGAttributes<SVGSVGElement>) => (<Icon {...props}>
    <path d="M21 14.5A8.5 8.5 0 0 1 9.5 3 7 7 0 1 0 21 14.5z"/>
  </Icon>);
export const IconUser = (props: SVGAttributes<SVGSVGElement>) => (<Icon {...props}>
    <path d="M20 21a8 8 0 0 0-16 0"/>
    <circle cx={12} cy={7} r={4}/>
  </Icon>);
export const IconCalendar = (props: SVGAttributes<SVGSVGElement>) => (<Icon {...props}>
    <rect x={3} y={4} width={18} height={18} rx={2}/>
    <path d="M16 2v4M8 2v4M3 10h18"/>
  </Icon>);
export const IconClipboard = (props: SVGAttributes<SVGSVGElement>) => (<Icon {...props}>
    <path d="M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2"/>
    <path d="M9 5a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2v0a2 2 0 0 1-2 2h-2a2 2 0 0 1-2-2v0z"/>
  </Icon>);
export const IconUsers = (props: SVGAttributes<SVGSVGElement>) => (<Icon {...props}>
    <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
    <circle cx={9} cy={7} r={4}/>
    <path d="M23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75"/>
  </Icon>);
export const IconPackage = (props: SVGAttributes<SVGSVGElement>) => (<Icon {...props}>
    <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/>
    <path d="M3.27 6.96 12 12.01l8.73-5.05M12 22.08V12"/>
  </Icon>);
export const IconGraduation = (props: SVGAttributes<SVGSVGElement>) => (<Icon {...props}>
    <path d="M22 10v6M6 6 3 8l9 5 9-5-9-5-6 3-3-2v8"/>
    <path d="M6 12h.01M6 16l4 2 8-4"/>
  </Icon>);
export const IconLogOut = (props: SVGAttributes<SVGSVGElement>) => (<Icon {...props}>
    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9"/>
  </Icon>);
export const IconPlus = (props: SVGAttributes<SVGSVGElement>) => (<Icon {...props}>
    <path d="M12 5v14M5 12h14"/>
  </Icon>);
