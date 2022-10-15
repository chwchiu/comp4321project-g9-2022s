<%@page import="com.project.app.SearchEngine, com.project.app.SearchResult, java.util.ArrayList" %>
<html>
  <HEAD>
    <TITLE> Search Engine </TITLE>
  </HEAD>
  <BODY> 
    <a class="back-button" href="./search.html">
      <img src="./back-arrow.svg" style="width: 20px; height: 20px;">
    </a>
    <div class="header">
      <p class="logo center">
        <% java.util.Date date = new java.util.Date(); %>
        <img src="./bingleLogo.png" alt="logo">
      </p>
      <div class="center">
        <label class="label">
          <input
            disabled
            class="searchBox"
            value='<%out.println(request.getParameter("txtname"));%>'
            type="text"
            name="txtname"
          />
        </label>
      </div>
    </div>

    <% SearchEngine se = new SearchEngine(request.getParameter("txtname"));
    ArrayList<SearchResult> result = se.search();
    for(SearchResult res: result)
    {
    %>
      <div class="searchResults"> 
        <p class="url"><%out.println(res.getUrl());%></p> 
        <a class="title" href="<%out.println(res.getUrl());%>" style="font-weight:bold"><%out.println(res.getTitle());%></a>
        <p class="score">Similarity Score: <%out.println(res.getScore());%></p>
        <p class="last-mod">Last Modified: <%out.println(res.getLastMod());%> </p>
      </div>
    <%
    }  
    %>
  </BODY>
</html>

<style> 
  .center {
    display: block;
    margin-left: auto;
    margin-right: auto;
  }

  .header {
    display: flex;
    flex-direction: column;
    flex-wrap: nowrap;

  }
  .searchResults {
    display: flex; 
    flex-direction: column;
    flex-wrap: nowrap;
    margin-top: 20px;
    margin-bottom: 20px;
  }

  .url {
    font-size: 12px;
    color: rgb(53, 136, 20);
    padding: 0;
    margin: 0;
  }

  .title {
    font-size: 15px;
    text-decoration: none;
    color: blue;
    padding: 0;
    margin: 0;
  }

  .last-mod {
    font-size: 12px;
    padding:0;
    margin: 0; 
  }
  .score {
    font-size: 12px;
    padding: 0;
    margin: 0;
  }

  .child {
    font-size: 12px;
    padding: 0;
    margin: 0;
  }
  .label {
    position: relative;
  }

  .label:before {
    content: "";
    position: absolute;
    left: 10px;
    top: 0.5px;
    bottom: 0;
    width: 30px;
    height: 30px;
    background: url("./searchIcon.svg");
  }

  .searchBox {
    padding: 10px 50px;
    margin: 10px 0;
    border: 2px solid #eee;
    border-radius: 10px;
    width: 500px;
    box-shadow: 0 0 15px 4px rgba(0, 0, 0, 0.06);
  }

</style> 