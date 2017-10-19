var url = 'http://localhost:9000/pdf/df629b06-e53a-4e9f-87ed-9b771adc9b14/VBA-21-0966-ARE';
// Asynchronous download of PDF
var loadingTask = PDFJS.getDocument(url);
loadingTask.promise.then(function (pdf) {
    console.log('PDF loaded');

    // Fetch the first page
    var pageNumber = 1;
    pdf.getPage(pageNumber).then(function (page) {
        console.log('Page loaded');

        var scale = 2;
        var viewport = page.getViewport(scale);

        // Prepare canvas using PDF page dimensions
        var canvas = document.getElementById('pdf-canvas');
        var context = canvas.getContext('2d');
        canvas.height = viewport.height;
        canvas.width = viewport.width;

        // Render PDF page into canvas context
        var renderContext = {
            canvasContext: context,
            viewport: viewport
        };
        var renderTask = page.render(renderContext);
        renderTask.then(function () {
            console.log('Page rendered');
        });
    });
}, function (reason) {
    // PDF loading error
    console.error(reason);
});
