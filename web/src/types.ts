export type ViewId =
  | "home"
  | "overview"
  | "treatments"
  | "research"
  | "resources"
  | "glossary"
  | "about"
  | "disclaimer";

export interface Route {
  view: ViewId;
  query: string;
}

export interface NamedLink {
  label: string;
  url: string;
}

export interface EducationSection {
  id: string;
  title: string;
  body: string;
  glossaryTerms: string[];
}

export interface EducationPage {
  title: string;
  intro: string;
  callout: string;
  sections: EducationSection[];
  sources: NamedLink[];
}

export interface ResearchItem {
  id: string;
  title: string;
  summary: string;
  source: string;
  sourceUrl: string;
  date: string;
  kind: string;
}

export interface ResearchPage {
  title: string;
  howToRead: string;
  askSpecialist: string;
  pipelineThemes: EducationSection[];
  clinicalTrialsPrimer: string;
  exploreTrialsLabel: string;
  exploreTrialsUrl: string;
  notableDevelopments: ResearchItem[];
  trialItems: ResearchItem[];
}

export interface ResourceItem {
  title: string;
  description: string;
  url: string;
  category: string;
}

export interface ResourcePage {
  intro: string;
  items: ResourceItem[];
}

export interface GlossaryTerm {
  term: string;
  definition: string;
}

export interface Disclaimer {
  shortText: string;
  fullText: string;
}

export interface About {
  purpose: string;
  sourcing: string;
  refresh: string;
  version: string;
}

export interface Catalog {
  contentVersion: string;
  generatedAt: string;
  educationLastReviewed: string;
  overview: EducationPage;
  treatments: EducationPage;
  research: ResearchPage;
  resources: ResourcePage;
  glossary: GlossaryTerm[];
  disclaimer: Disclaimer;
  about: About;
}
