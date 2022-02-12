import React from 'react';
import './App.css';
import '../node_modules/bootstrap/dist/css/bootstrap.css';

function App() {
  const queryParams = new URLSearchParams(window.location.search);
  const chatId = queryParams.get('chatId');

  function download() {
    window.location.href = `http://127.0.0.1:8091/api/keeper-bot/download?chatId=${chatId}`;
  }

  return (
      <div className="App">
        <h1 className="display-1"> Click to button for download</h1>
        <button className="btn btn-primary" onClick={() => download()}>Download</button>
      </div>
  );
}

export default App;