# Use Case: Comment On Post

## Main Flow

### Preconditions:
* The user is registered.
* The user is authenticated.
* The post exists.

### Main Event Flow:
1. The user writes comment.
2. The user clicks "save".
3. The system checks that the comment is not empty and does not exceed the word limit.
4. The system saves the comment.
5. The system displays comment under the post.


## Alternative Event Flow:
### A1. Word Limit Exceeded
* At step 3, if the comment surpasses the word limit.
1. The system notifies the user by displaying the error message "word limit exceeded".
2. The system does not save the comment
3. The user shortens or cancels the comment.

### A2. Empty Comment
* At step 3, if the comment is empty.
1. The system notifies the user by displaying the error message "empty comment".
2. The system does not save comment.
3. The user add words or cancels the comment.