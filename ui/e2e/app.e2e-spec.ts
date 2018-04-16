import { AppPage } from './app.po';

describe('Vetafi App', () => {
  let page: AppPage;

  beforeEach(() => {
    page = new AppPage();
  });

  it('should display homepage', () => {
    page.navigateTo();
  });
});
